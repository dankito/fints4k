package net.dankito.banking

import net.dankito.banking.model.AccountCredentials
import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.tan.FlickerCodeTanChallenge
import net.dankito.banking.ui.model.tan.ImageTanChallenge
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanImage
import net.dankito.banking.util.hbci4jModelMapper
import org.kapott.hbci.callback.AbstractHBCICallback
import org.kapott.hbci.callback.HBCICallback
import org.kapott.hbci.manager.HBCIUtils
import org.kapott.hbci.manager.MatrixCode
import org.kapott.hbci.manager.QRCode
import org.kapott.hbci.passport.HBCIPassport
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Ueber diesen Callback kommuniziert HBCI4Java mit dem Benutzer und fragt die benoetigten
 * Informationen wie Benutzerkennung, PIN usw. ab.
 */
open class HbciCallback(
    protected val credentials: AccountCredentials,
    protected val customer: Customer,
    protected val mapper: hbci4jModelMapper,
    protected val callback: BankingClientCallback
) : AbstractHBCICallback() {

    companion object {
        private val log = LoggerFactory.getLogger(HbciCallback::class.java)
    }


    /**
     * @see org.kapott.hbci.callback.HBCICallback.log
     */
    override fun log(msg: String, level: Int, date: Date, trace: StackTraceElement) {
        // Ausgabe von Log-Meldungen bei Bedarf
        when (level) {
            HBCIUtils.LOG_ERR -> log.error(msg)
            HBCIUtils.LOG_WARN -> log.warn(msg)
            HBCIUtils.LOG_INFO-> log.info(msg)
            HBCIUtils.LOG_DEBUG, HBCIUtils.LOG_DEBUG2 -> log.debug(msg)
            else -> log.trace(msg)
        }
    }

    /**
     * @see org.kapott.hbci.callback.HBCICallback.callback
     */
    override fun callback(passport: HBCIPassport, reason: Int, msg: String, datatype: Int, retData: StringBuffer) {
        log.info("Callback: [$reason] $msg ($retData)") // TODO: remove again

        // Diese Funktion ist wichtig. Ueber die fragt HBCI4Java die benoetigten Daten von uns ab.
        when (reason) {
            // Mit dem Passwort verschluesselt HBCI4Java die Passport-Datei.
            // Wir nehmen hier der Einfachheit halber direkt die PIN. In der Praxis
            // sollte hier aber ein staerkeres Passwort genutzt werden.
            // Die Ergebnis-Daten muessen in dem StringBuffer "retData" platziert werden.
            // if you like or need to change your pin, return your old one for NEED_PASSPHRASE_LOAD and your new
            // one for NEED_PASSPHRASE_SAVE
            HBCICallback.NEED_PASSPHRASE_LOAD, HBCICallback.NEED_PASSPHRASE_SAVE -> retData.replace(0, retData.length, credentials.password)


            /*      Customer (authentication) data           */

            // BLZ wird benoetigt
            HBCICallback.NEED_BLZ -> retData.replace(0, retData.length, credentials.bankCode)

            // Die Benutzerkennung
            HBCICallback.NEED_USERID -> retData.replace(0, retData.length, credentials.customerId)

            // Die Kundenkennung. Meist identisch mit der Benutzerkennung.
            // Bei manchen Banken kann man die auch leer lassen
            HBCICallback.NEED_CUSTOMERID -> retData.replace(0, retData.length, credentials.customerId)

            // PIN wird benoetigt
            HBCICallback.NEED_PT_PIN -> retData.replace(0, retData.length, credentials.password)


            /*          TAN         */

            // ADDED: Auswaehlen welches PinTan Verfahren verwendet werden soll
            HBCICallback.NEED_PT_SECMECH -> selectTanProcedure(retData.toString())?.let { selectedTanProcedure ->
                customer.selectedTanProcedure = selectedTanProcedure
                retData.replace(0, retData.length, selectedTanProcedure.bankInternalProcedureCode)
            }

            // chipTan or simple TAN request (iTAN, smsTAN, ...)
            HBCICallback.NEED_PT_TAN -> {
                getTanFromUser(customer, msg, retData.toString())?.let { enteredTan ->
                    retData.replace(0, retData.length, enteredTan)
                }
            }

            // chipTAN-QR
            HBCICallback.NEED_PT_QRTAN -> { // use class QRCode to display QR code
                val qrData = retData.toString()
                val qrCode = QRCode(qrData, msg)
                val enterTanResult = callback.enterTan(customer, ImageTanChallenge(TanImage(qrCode.mimetype, qrCode.image), msg, customer.selectedTanProcedure!!))
                enterTanResult.enteredTan?.let { enteredTan ->
                    retData.replace(0, retData.length, enteredTan)
                }
            }

            // photoTan
            HBCICallback.NEED_PT_PHOTOTAN -> { // use class MatrixCode to display photo
                val matrixCode = MatrixCode(retData.toString())
                val enterTanResult = callback.enterTan(customer, ImageTanChallenge(TanImage(matrixCode.mimetype, matrixCode.image), msg, customer.selectedTanProcedure!!))
                enterTanResult.enteredTan?.let { enteredTan ->
                    retData.replace(0, retData.length, enteredTan)
                }
            }

            // smsTan: Select cell phone to which SMS should be send
            HBCICallback.NEED_PT_TANMEDIA -> {
                log.info("TODO: select cell phone: $msg ($retData)")
            }

            // wrong pin entered -> inform user
            HBCICallback.WRONG_PIN -> {
                log.info("TODO: user entered wrong pin: $msg ($retData)")
            }

            // UserId changed -> inform user
            HBCICallback.USERID_CHANGED -> { // im Parameter retData stehen die neuen Daten im Format UserID|CustomerID drin
                log.info("TODO: UserId changed: $msg ($retData)")
            }

            // user entered wrong Banleitzahl or Kontonummer -> inform user
            HBCICallback.HAVE_CRC_ERROR -> { // retData contains wrong values in form "BLZ|KONTONUMMER". Set correct ones in the same form in retData
                log.info("TODO: wrong Banleitzahl or Kontonummer entered: $msg ($retData)")
            }

            // user entered wrong IBAN -> inform user
            HBCICallback.HAVE_IBAN_ERROR -> { // retData contains wrong IBAN. Set correct IBAN in retData
                log.info("TODO: wrong IBAN entered: $msg ($retData)")
            }

            // message from bank to user. should get displayed to user
            HBCICallback.HAVE_INST_MSG -> {
                // TODO: inform user
                log.error("TODO: inform user, received a message from bank: $msg\n$retData")
            }

            // Manche Fehlermeldungen werden hier ausgegeben
            HBCICallback.HAVE_ERROR -> { // to ignore error set an empty String in retData
                // TODO: inform user
                log.error("TODO: inform user, error occurred: $msg\n$retData")
            }

            else -> { // Wir brauchen nicht alle der Callbacks
            }
        }
    }

    /**
     * @see org.kapott.hbci.callback.HBCICallback.status
     */
    override fun status(passport: HBCIPassport, statusTag: Int, o: Array<Any>?) {
        // So aehnlich wie log(String,int,Date,StackTraceElement) jedoch fuer Status-Meldungen.
        val param = if (o == null) "" else o.joinToString()

        when (statusTag) {
            HBCICallback.STATUS_MSG_RAW_SEND -> log.debug("Sending message:\n$param")
            HBCICallback.STATUS_MSG_RAW_RECV -> log.debug("Received message:\n$param")
//            else -> log.debug("New status [$statusTag]: $param")
        }
    }


    open fun getTanFromUser(customer: Customer, messageToShowToUser: String, challengeHHD_UC: String): String? {
        // Wenn per "retData" Daten uebergeben wurden, dann enthalten diese
        // den fuer chipTAN optisch zu verwendenden Flickercode.
        // Falls nicht, ist es eine TAN-Abfrage, fuer die keine weiteren
        // Parameter benoetigt werden (z.B. smsTAN, iTAN oder aehnliches)

        // Die Variable "msg" aus der Methoden-Signatur enthaelt uebrigens
        // den bankspezifischen Text mit den Instruktionen fuer den User.
        // Der Text aus "msg" sollte daher im Dialog dem User angezeigt
        // werden.

        val enterTanResult = if (challengeHHD_UC.isNullOrEmpty()) {
            callback.enterTan(customer, TanChallenge(messageToShowToUser, customer.selectedTanProcedure!!))
        }
        else {
            // for Sparkasse messageToShowToUser started with "chipTAN optisch\nTAN-Nummer\n\n"
            val usefulMessage = messageToShowToUser.split("\n").last().trim()

//            val parsedDataSet = FlickerCode(challengeHHD_UC).render()
            callback.enterTan(customer, FlickerCodeTanChallenge(net.dankito.banking.ui.model.tan.FlickerCode("", challengeHHD_UC), usefulMessage, customer.selectedTanProcedure!!))
        }

        return enterTanResult.enteredTan
    }



    open fun selectTanProcedure(supportedTanProceduresString: String): net.dankito.banking.ui.model.tan.TanProcedure? {
        val supportedTanProcedures = mapper.mapTanProcedures(supportedTanProceduresString)

        customer.supportedTanProcedures = supportedTanProcedures

        if (supportedTanProcedures.isNotEmpty()) {
            // select any procedure, user then can select her preferred one in EnterTanDialog; try not to select 'chipTAN manuell'
            return supportedTanProcedures.firstOrNull { it.displayName.contains("manuell", true) == false }
                ?: supportedTanProcedures.firstOrNull()
        }

        return null
    }

}