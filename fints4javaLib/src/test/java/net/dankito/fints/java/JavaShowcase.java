package net.dankito.fints.java;

import net.dankito.fints.FinTsClient;
import net.dankito.fints.FinTsClientCallback;
import net.dankito.fints.banks.BankFinder;
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium;
import net.dankito.fints.model.*;
import net.dankito.fints.model.mapper.BankDataMapper;
import net.dankito.fints.response.client.AddAccountResponse;
import net.dankito.fints.response.client.FinTsClientResponse;
import net.dankito.fints.response.client.GetTransactionsResponse;
import net.dankito.fints.util.Java8Base64Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class JavaShowcase {

    public static void main(String[] args) {
        BankFinder bankFinder = new BankFinder();

        // set your bank code (Bankleitzahl) here. Or create BankData manually. Required fields are:
        // bankCode, bankCountryCode (Germany = 280), finTs3ServerAddress and for bank transfers bic
        List<BankInfo> foundBanks = bankFinder.findBankByBankCode("<bank_code>");

        if (foundBanks.isEmpty() == false) {
            BankData bank = new BankDataMapper().mapFromBankInfo(foundBanks.get(0));
            // set your customer data (customerId = Kontonummer in most cases, pin = online banking pin)
            CustomerData customer = new CustomerData("<customer_id>", "<pin>");

            FinTsClientCallback callback = new FinTsClientCallback() {

                @Nullable
                @Override
                public TanProcedure askUserForTanProcedure(@NotNull List<? extends TanProcedure> supportedTanProcedures, @Nullable TanProcedure suggestedTanProcedure) {
                    return suggestedTanProcedure; // simply return suggestedTanProcedure as in most cases it's the best fitting one
                }

                @Nullable
                @Override
                public EnterTanResult enterTan(@NotNull CustomerData customer, @NotNull TanChallenge tanChallenge) {
                    return EnterTanResult.Companion.userDidNotEnterTan();
                }

                @Nullable
                @Override
                public EnterTanGeneratorAtcResult enterTanGeneratorAtc(@NotNull CustomerData customer, @NotNull TanGeneratorTanMedium tanMedium) {
                    return EnterTanGeneratorAtcResult.Companion.userDidNotEnterTan();
                }
            };

            FinTsClient finTsClient = new FinTsClient(callback, new Java8Base64Service());

            AddAccountResponse addAccountResponse = finTsClient.addAccount(bank, customer);
            if (addAccountResponse.isSuccessful()) {
                System.out.println("Could not add account for " + bank.getBankCode() + " " + customer.getCustomerId() + ":");
                showResponseError(addAccountResponse);
                return;
            }

            System.out.println("Successfully added account for " + bank.getBankCode() + " " + customer.getCustomerId());

            /*      Other stuff you can do with the lib         */

            for (AccountData account : customer.getAccounts()) { // accounts are now retrieved
                if (account.getSupportsRetrievingAccountTransactions()) {
                    // retrieves all account transactions, but requires entering a TAN (FinTsClientCallback.enterTan() will be called)
//                    GetTransactionsResponse response = finTsClient.getTransactions(
//                            new GetTransactionsParameter(true), bank, customer, account);
//
//                    showGetTransactionsResponse(response);
                }

                if (account.getSupportsTransferringMoney()) {
                    // transfer 0.01 € to yourself
//                    BankTransferData data = new BankTransferData(customer.getName(), account.getIban(), bank.getBic(), new BigDecimal("0.01"), "Hey I can transfer money to myself")
//                    FinTsClientResponse transferMoneyResponse = finTsClient.doBankTransfer(data, bank, customer, account);
//
//                    if (transferMoneyResponse.isSuccessful()) {
//                        System.out.println("Successfully transferred " + data.getAmount() + " to " + data.getCreditorIban());
//                    }
//                    else {
//                        showResponseError(transferMoneyResponse);
//                    }
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
