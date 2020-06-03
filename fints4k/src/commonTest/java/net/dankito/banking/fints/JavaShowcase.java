package net.dankito.banking.fints;

import net.dankito.banking.fints.banks.IBankFinder;
import net.dankito.banking.fints.banks.InMemoryBankFinder;
import net.dankito.banking.fints.callback.FinTsClientCallback;
import net.dankito.banking.fints.callback.SimpleFinTsClientCallback;
import net.dankito.banking.fints.model.AccountData;
import net.dankito.banking.fints.model.AccountFeature;
import net.dankito.banking.fints.model.AccountTransaction;
import net.dankito.banking.fints.model.BankData;
import net.dankito.banking.bankfinder.BankInfo;
import net.dankito.banking.fints.model.BankTransferData;
import net.dankito.banking.fints.model.CustomerData;
import net.dankito.banking.fints.model.EnterTanGeneratorAtcResult;
import net.dankito.banking.fints.model.EnterTanResult;
import net.dankito.banking.fints.model.TanChallenge;
import net.dankito.banking.fints.model.TanProcedure;
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium;
import net.dankito.banking.fints.model.mapper.BankDataMapper;
import net.dankito.banking.fints.response.client.AddAccountResponse;
import net.dankito.banking.fints.response.client.FinTsClientResponse;
import net.dankito.banking.fints.response.client.GetTransactionsResponse;
import net.dankito.banking.fints.util.Java8Base64Service;

import java.math.BigDecimal;
import java.util.List;


public class JavaShowcase {

    public static void main(String[] args) {
        JavaShowcase showcase = new JavaShowcase();

        showcase.basicShowcase();

        showcase.advancedShowcase();
    }

    private void basicShowcase() {
        // Set your bank code (Bankleitzahl) here.
        // BankInfo contains e.g. a bank's FinTS server address, country code and BIC (needed for money transfer)
        List<BankInfo> foundBanks = new InMemoryBankFinder().findBankByNameBankCodeOrCity("<bank code, bank name or city>");

        if (foundBanks.isEmpty() == false) { // please also check if bank supports FinTS 3.0
            BankData bank = new BankDataMapper().mapFromBankInfo(foundBanks.get(0));

            // set your customer data (customerId = username you use to log in; pin = online banking pin / password)
            CustomerData customer = new CustomerData("<customer_id>", "<pin>");

            FinTsClientCallback callback = new SimpleFinTsClientCallback(); // see advanced showcase for configuring callback

            FinTsClient finTsClient = new FinTsClient(callback, new Java8Base64Service());

            AddAccountResponse addAccountResponse = finTsClient.addAccount(bank, customer);

            if (addAccountResponse.isSuccessful()) {
                System.out.println("Successfully added account for " + bank.getBankCode() + " " + customer.getCustomerId());

                if (addAccountResponse.getBookedTransactions().isEmpty() == false) {
                    System.out.println("Account transactions of last 90 days:");
                    showGetTransactionsResponse(addAccountResponse);
                }
            }
            else {
                System.out.println("Could not add account for " + bank.getBankCode() + " " + customer.getCustomerId() + ":");
                showResponseError(addAccountResponse);
            }

            // see advanced show case what else you can do with this library, e.g. retrieving all account transactions and transferring money
        }
    }

    private void advancedShowcase() {
        IBankFinder bankFinder = new InMemoryBankFinder();

        // Set your bank code (Bankleitzahl) here. Or create BankData manually. Required fields are:
        // bankCode, bankCountryCode (Germany = 280), finTs3ServerAddress and for bank transfers bic
        List<BankInfo> foundBanks = bankFinder.findBankByBankCode("<bank_code>");

        if (foundBanks.isEmpty() == false) { // please also check if bank supports FinTS 3.0
            BankData bank = new BankDataMapper().mapFromBankInfo(foundBanks.get(0));

            // set your customer data (customerId = Kontonummer in most cases, pin = online banking pin)
            CustomerData customer = new CustomerData("<customer_id>", "<pin>");

            FinTsClientCallback callback = new FinTsClientCallback() {

                @Override
                public TanProcedure askUserForTanProcedure(List<? extends TanProcedure> supportedTanProcedures, TanProcedure suggestedTanProcedure) {
                    // E.g. show a dialog to ask for user's TAN procedure.
                    // In most cases it's senseful to simply return suggestedTanProcedure and to let
                    // user select TAN procedure when entering TAN is required (see enterTan() below)
                    return suggestedTanProcedure;
                }

                @Override
                public EnterTanResult enterTan(CustomerData customer, TanChallenge tanChallenge) {
                    // e.g. show
                    // - Android: net.dankito.banking.ui.android.dialogs.EnterTanDialog
                    // - JavaFX: net.dankito.banking.ui.javafx.dialogs.tan.EnterTanDialog
                    return EnterTanResult.Companion.userDidNotEnterTan(); // user did not enter TAN. aborts operation
                }

                @Override
                public EnterTanGeneratorAtcResult enterTanGeneratorAtc(CustomerData customer, TanGeneratorTanMedium tanMedium) {
                    // needed only in rare cases to synchronize TAN generator for chipTAN procedures. E.g. show
                    // - Android: net.dankito.banking.ui.android.dialogs.EnterAtcDialog
                    return EnterTanGeneratorAtcResult.Companion.userDidNotEnterAtc(); // user did not enter TAN and ATC. aborts operation
                }
            };


            // may also check if FinTsClientForCustomer fits your needs, avoids passing bank and customer to each method
            FinTsClient finTsClient = new FinTsClient(callback, new Java8Base64Service());

            AddAccountResponse addAccountResponse = finTsClient.addAccount(bank, customer);
            if (addAccountResponse.isSuccessful() == false) {
                System.out.println("Could not add account for " + bank.getBankCode() + " " + customer.getCustomerId() + ":");
                showResponseError(addAccountResponse);
                return;
            }

            System.out.println("Successfully added account for " + bank.getBankCode() + " " + customer.getCustomerId());


            for (AccountData account : customer.getAccounts()) { // accounts are now retrieved
                if (account.supportsFeature(AccountFeature.RetrieveAccountTransactions)) {
                    // Most banks support retrieving account transactions of last 90 without TAN, may also your bank.
                    // Alternatively call getTransactions() to retrieve all account transactions. But then entering a TAN is required.
                    GetTransactionsResponse response = finTsClient.tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, account);

                    showGetTransactionsResponse(response);
                }

                if (account.supportsFeature(AccountFeature.TransferMoney)) {
                    // Transfer 0.01 € to yourself. Transferring money to one self doesn't require a TAN.
                    BankTransferData data = new BankTransferData(customer.getName(), account.getIban(), bank.getBic(),
                            new BigDecimal("0.01"), "Give me some money", false);
                    FinTsClientResponse transferMoneyResponse = finTsClient.doBankTransfer(data, bank, customer, account);

                    if (transferMoneyResponse.isSuccessful()) {
                        System.out.println("Successfully transferred " + data.getAmount() + " to " + data.getCreditorIban());
                    }
                    else {
                        showResponseError(transferMoneyResponse);
                    }
                }
            }
        }
    }

    private static void showGetTransactionsResponse(GetTransactionsResponse response) {
        if (response.isSuccessful()) {
            System.out.println("Balance (Saldo) = " + response.getBalance());

            System.out.println("Account transactions (Umsätze):");
            for (AccountTransaction transaction : response.getBookedTransactions()) {
                System.out.println(transaction.toString());
            }
        }
        else {
            if (response.isStrongAuthenticationRequired()) {
                System.out.println("Sorry, your bank doesn't support retrieving account " +
                        "transactions of last 90 days without TAN");
            }
            else {
                System.out.println("An error occurred:");
                showResponseError(response);
            }
        }
    }

    private static void showResponseError(FinTsClientResponse response) {
        if (response.getException() != null) { // something severe occurred
            System.out.println(response.getException().getMessage());
        }

        // error messages retrieved from bank (e.g. PIN is wrong, message contains errors, ...)
        for (String retrievedErrorMessage : response.getErrorsToShowToUser()) {
            System.out.println(retrievedErrorMessage);
        }
    }

}
