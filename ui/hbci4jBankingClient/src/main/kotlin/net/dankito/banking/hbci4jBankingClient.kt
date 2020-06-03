package net.dankito.banking

import net.dankito.banking.model.AccountCredentials
import net.dankito.banking.model.ConnectResult
import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.util.AccountTransactionMapper
import net.dankito.banking.util.hbci4jModelMapper
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import org.kapott.hbci.GV.HBCIJob
import org.kapott.hbci.GV_Result.GVRKUms
import org.kapott.hbci.GV_Result.GVRSaldoReq
import org.kapott.hbci.manager.HBCIHandler
import org.kapott.hbci.manager.HBCIUtils
import org.kapott.hbci.manager.HBCIVersion
import org.kapott.hbci.passport.AbstractHBCIPassport
import org.kapott.hbci.passport.HBCIPassport
import org.kapott.hbci.status.HBCIExecStatus
import org.kapott.hbci.structures.Value
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


open class hbci4jBankingClient(
    bankInfo: BankInfo,
    customerId: String,
    pin: String,
    protected val dataFolder: File,
    protected val threadPool: IThreadPool = ThreadPool(),
    protected val callback: BankingClientCallback
) : IBankingClient {

    companion object {
        // the date format is hard coded in HBCIUtils.string2DateISO()
        val HbciLibDateFormat = SimpleDateFormat("yyyy-MM-dd")

        const val NinetyDaysInMilliseconds = 90 * 24 * 60 * 60 * 1000L

        private val log = LoggerFactory.getLogger(hbci4jBankingClient::class.java)
    }


    protected val credentials = AccountCredentials(bankInfo.bankCode, customerId, pin)

    protected var bank = Bank(bankInfo.name, bankInfo.bankCode, bankInfo.bic, bankInfo.pinTanAddress ?: "")

    protected var account = Account(bank, customerId, pin, "")


    protected val mapper = hbci4jModelMapper()

    protected val accountTransactionMapper = AccountTransactionMapper()


    override val messageLogWithoutSensitiveData: List<MessageLogEntry> = listOf() // TODO: implement


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        threadPool.runAsync {
            callback(addAccount())
        }
    }

    open fun addAccount(): AddAccountResponse {
        val connection = connect()
        closeConnection(connection)

        if(connection.successful) {
            connection.passport?.let { passport ->
                val accounts = passport.accounts
                if (accounts == null || accounts.size == 0) {
                    log.error("Keine Konten ermittelbar")
                    return AddAccountResponse(false, "Keine Konten ermittelbar", account) // TODO: translate
                }

                this.account.bankAccounts = mapper.mapBankAccounts(account, accounts, passport)

                return tryToRetrieveAccountTransactionsForAddedAccounts(account)
            }
        }

        return AddAccountResponse(false, null, account, error = connection.error)
    }

    protected open fun tryToRetrieveAccountTransactionsForAddedAccounts(account: Account): AddAccountResponse {
        val transactionsOfLast90DaysResponses = mutableListOf<GetTransactionsResponse>()
        val balances = mutableMapOf<BankAccount, BigDecimal>()
        val bookedTransactions = mutableMapOf<BankAccount, List<AccountTransaction>>()
        val unbookedTransactions = mutableMapOf<BankAccount, List<Any>>()

        account.bankAccounts.forEach { bankAccount ->
            if (bankAccount.supportsRetrievingAccountTransactions) {
                val response = getTransactionsOfLast90Days(bankAccount)
                transactionsOfLast90DaysResponses.add(response)

                bookedTransactions.put(bankAccount, response.bookedTransactions)
                unbookedTransactions.put(bankAccount, response.unbookedTransactions)
                balances.put(bankAccount, response.balance ?: BigDecimal.ZERO) // TODO: really add BigDecimal.Zero if balance couldn't be retrieved?
            }
        }

        val supportsRetrievingTransactionsOfLast90DaysWithoutTan = transactionsOfLast90DaysResponses.firstOrNull { it.isSuccessful } != null

        return AddAccountResponse(true, null, account, supportsRetrievingTransactionsOfLast90DaysWithoutTan,
            bookedTransactions, unbookedTransactions, balances)
    }


    /**
     * According to PSD2 for the accounting entries of the last 90 days the two-factor authorization does not have to
     * be applied. It depends on the bank if they request a second factor or not.
     *
     * So we simply try to retrieve at accounting entries of the last 90 days and see if a second factor is required
     * or not.
     */
    open fun getTransactionsOfLast90DaysAsync(bankAccount: BankAccount, callback: (GetTransactionsResponse) -> Unit) {
        threadPool.runAsync {
            callback(getTransactionsOfLast90Days(bankAccount))
        }
    }

    /**
     * According to PSD2 for the accounting entries of the last 90 days the two-factor authorization does not have to
     * be applied. It depends on the bank if they request a second factor or not.
     *
     * So we simply try to retrieve at accounting entries of the last 90 days and see if a second factor is required
     * or not.
     */
    open fun getTransactionsOfLast90Days(bankAccount: BankAccount): GetTransactionsResponse {
        val ninetyDaysAgo = Date(Date().time - NinetyDaysInMilliseconds)

        return getTransactions(bankAccount, GetTransactionsParameter(bankAccount.supportsRetrievingBalance, ninetyDaysAgo)) // TODO: implement abortIfTanIsRequired
    }

    override fun getTransactionsAsync(bankAccount: BankAccount, parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
        threadPool.runAsync {
            callback(getTransactions(bankAccount, parameter))
        }
    }

    protected open fun getTransactions(bankAccount: BankAccount, parameter: GetTransactionsParameter): GetTransactionsResponse {
        val connection = connect()

        connection.handle?.let { handle ->
            try {
                val (nullableBalanceJob, accountTransactionsJob, status) = executeJobsForGetAccountingEntries(handle, bankAccount, parameter)

                // Pruefen, ob die Kommunikation mit der Bank grundsaetzlich geklappt hat
                if (!status.isOK) {
                    log.error("Could not connect to bank ${credentials.bankCode} ${status.toString()}: ${status.errorString}")
                    return GetTransactionsResponse(bankAccount, false, null, error = Exception("Could not connect to bank ${credentials.bankCode}: ${status.toString()}"))
                }

                // Auswertung des Saldo-Abrufs.
                var balance = BigDecimal.ZERO
                if (parameter.alsoRetrieveBalance && nullableBalanceJob != null) {
                    val balanceResult = nullableBalanceJob.jobResult as GVRSaldoReq
                    if(balanceResult.isOK == false) {
                        log.error("Could not fetch balance of bank account $bankAccount: $balanceResult", balanceResult.getJobStatus().exceptions)
                        return GetTransactionsResponse(bankAccount, false, null, error = Exception("Could not fetch balance of bank account $bankAccount: $balanceResult"))
                    }

                    balance = balanceResult.entries[0].ready.value.bigDecimalValue
                }


                // Das Ergebnis des Jobs koennen wir auf "GVRKUms" casten. Jobs des Typs "KUmsAll"
                // liefern immer diesen Typ.
                val result = accountTransactionsJob.jobResult as GVRKUms

                // Pruefen, ob der Abruf der Umsaetze geklappt hat
                if (result.isOK == false) {
                    log.error("Could not get fetch account transactions of bank account $bankAccount: $result", result.getJobStatus().exceptions)
                    return GetTransactionsResponse(bankAccount, false, null, error = Exception("Could not fetch account transactions of bank account $bankAccount: $result"))
                }

                return GetTransactionsResponse(bankAccount, true, null, accountTransactionMapper.mapAccountTransactions(bankAccount, result),
                    listOf(), balance)
            }
            catch(e: Exception) {
                log.error("Could not get accounting details for bank ${credentials.bankCode}", e)
                return GetTransactionsResponse(bankAccount, false, null, error = e)
            }
            finally {
                closeConnection(connection)
            }
        }

        closeConnection(connection)

        return GetTransactionsResponse(bankAccount, false, null, error = connection.error)
    }

    protected open fun executeJobsForGetAccountingEntries(handle: HBCIHandler, bankAccount: BankAccount, parameter: GetTransactionsParameter): Triple<HBCIJob?, HBCIJob, HBCIExecStatus> {
        val konto = mapper.mapToKonto(bank, bankAccount)

        // 1. Auftrag fuer das Abrufen des Saldos erzeugen
        var balanceJob: HBCIJob? = null
        if (parameter.alsoRetrieveBalance) {
            val createdBalanceJob = handle.newJob("SaldoReq")
            createdBalanceJob.setParam("my", konto) // festlegen, welches Konto abgefragt werden soll.
            createdBalanceJob.addToQueue() // Zur Liste der auszufuehrenden Auftraege hinzufuegen

            balanceJob = createdBalanceJob
        }
        // 2. Auftrag fuer das Abrufen der Umsaetze erzeugen
        val accountTransactionsJob = handle.newJob("KUmsAll")
        accountTransactionsJob.setParam("my", konto) // festlegen, welches Konto abgefragt werden soll.
        // evtl. Datum setzen, ab welchem die Auszüge geholt werden sollen
        parameter.fromDate?.let {
            accountTransactionsJob.setParam("startdate", HbciLibDateFormat.format(it))
        }
        accountTransactionsJob.addToQueue() // Zur Liste der auszufuehrenden Auftraege hinzufuegen

        // Hier koennen jetzt noch weitere Auftraege fuer diesen Bankzugang hinzugefuegt
        // werden. Z.Bsp. Ueberweisungen.

        // Alle Auftraege aus der Liste ausfuehren.
        val status = handle.execute()

        return Triple(balanceJob, accountTransactionsJob, status)
    }


    override fun transferMoneyAsync(data: TransferMoneyData, bankAccount: BankAccount, callback: (BankingClientResponse) -> Unit) {
        threadPool.runAsync {
            callback(transferMoney(data, bankAccount))
        }
    }

    open fun transferMoney(data: TransferMoneyData, bankAccount: BankAccount): BankingClientResponse {
        val connection = connect()

        connection.handle?.let { handle ->
            try {
                createTransferCashJob(handle, data, bankAccount)

                val status = handle.execute()

                return BankingClientResponse(status.isOK, status.toString())
            } catch(e: Exception) {
                log.error("Could not transfer cash for account $bankAccount" , e)
                return BankingClientResponse(false, e.localizedMessage, e)
            }
            finally {
                closeConnection(connection)
            }
        }

        return BankingClientResponse(false, "Could not connect", connection.error)
    }

    protected open fun createTransferCashJob(handle: HBCIHandler, data: TransferMoneyData, bankAccount: BankAccount) {
        // TODO: implement instant payment
        val transferCashJob = handle.newJob("UebSEPA")

        val source = mapper.mapToKonto(bank, bankAccount)
        val destination = mapper.mapToKonto(data)
        val amount = Value(data.amount, "EUR")

        transferCashJob.setParam("src", source)
        transferCashJob.setParam("dst", destination)
        transferCashJob.setParam("btg", amount)
        transferCashJob.setParam("usage", data.usage)

        transferCashJob.addToQueue()
    }


    override fun restoreData() {
        // nothing to do for hbci4j
    }


    protected open fun connect(): ConnectResult {
        return connect(credentials, HBCIVersion.HBCI_300)
    }

    protected open fun connect(credentials: AccountCredentials, version: HBCIVersion): ConnectResult {
        // HBCI4Java initialisieren
        // In "props" koennen optional Kernel-Parameter abgelegt werden, die in der Klasse
        // org.kapott.hbci.manager.HBCIUtils (oben im Javadoc) beschrieben sind.
        val props = Properties()
        HBCIUtils.init(props, HbciCallback(credentials, account, mapper, callback))

        // In der Passport-Datei speichert HBCI4Java die Daten des Bankzugangs (Bankparameterdaten, Benutzer-Parameter, etc.).
        // Die Datei kann problemlos geloescht werden. Sie wird beim naechsten mal automatisch neu erzeugt,
        // wenn der Parameter "client.passport.PinTan.init" den Wert "1" hat (siehe unten).
        // Wir speichern die Datei der Einfachheit halber im aktuellen Verzeichnis.
        dataFolder.mkdirs()
        val passportFile = File(dataFolder, "passport_${credentials.bankCode}_${credentials.customerId}.dat")

        // Wir setzen die Kernel-Parameter zur Laufzeit. Wir koennten sie alternativ
        // auch oben in "props" setzen.
        HBCIUtils.setParam("client.passport.default", "PinTan") // Legt als Verfahren PIN/TAN fest.
        HBCIUtils.setParam("client.passport.PinTan.filename", passportFile.absolutePath)
        HBCIUtils.setParam("client.passport.PinTan.init", "1")

        var handle: HBCIHandler? = null
        var passport: HBCIPassport? = null

        try {
            // Erzeugen des Passport-Objektes.
            passport = AbstractHBCIPassport.getInstance()

            // Konfigurieren des Passport-Objektes.
            // Das kann alternativ auch alles ueber den Callback unten geschehen

            // Das Land.
            passport.country = "DE"

            // Server-Adresse angeben. Koennen wir entweder manuell eintragen oder direkt von HBCI4Java ermitteln lassen
            val info = HBCIUtils.getBankInfo(credentials.bankCode)
            passport.host = info.pinTanAddress

            // TCP-Port des Servers. Bei PIN/TAN immer 443, da das ja ueber HTTPS laeuft.
            passport.port = 443

            // Art der Nachrichten-Codierung. Bei Chipkarte/Schluesseldatei wird
            // "None" verwendet. Bei PIN/TAN kommt "Base64" zum Einsatz.
            passport.filterType = "Base64"

            // Verbindung zum Server aufbauen
            handle = HBCIHandler(version.getId(), passport)


        }
        catch(e: Exception) {
            log.error("Could not connect to bank ${credentials.bankCode}", e)
            closeConnection(handle, passport)

            return ConnectResult(false, e)
        }

        return ConnectResult(true, null, handle, passport)
    }

    protected open fun closeConnection(connection: ConnectResult) {
        closeConnection(connection.handle, connection.passport)
    }

    protected open fun closeConnection(handle: HBCIHandler?, passport: HBCIPassport?) {
        // Sicherstellen, dass sowohl Passport als auch Handle nach Beendigung geschlossen werden.
        try {
            handle?.close()

            passport?.close()

            HBCIUtils.doneThread() // i hate static variables, here's one of the reasons why: Old callbacks and therefore credentials get stored in static variables and therefor always the first entered credentials have been used
        } catch(e: Exception) { log.error("Could not close connection", e) }
    }

}