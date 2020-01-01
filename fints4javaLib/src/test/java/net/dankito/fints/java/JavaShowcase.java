package net.dankito.fints.java;

import net.dankito.fints.FinTsClient;
import net.dankito.fints.FinTsClientCallback;
import net.dankito.fints.banks.BankFinder;
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion;
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium;
import net.dankito.fints.model.*;
import net.dankito.fints.model.mapper.BankDataMapper;
import net.dankito.fints.response.client.GetTransactionsResponse;
import net.dankito.fints.util.Java8Base64Service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;


public class JavaShowcase {

    public static void main(String[] args) {
        BankFinder bankFinder = new BankFinder();

        // set your bank code (Bankleitzahl) here. Or create BankData manually. Required fields are:
        // bankCode, bankCountryCode (Germany = 280), finTs3ServerAddress and for bank transfers bic
        List<BankInfo> foundBanks = bankFinder.findBankByBankCode("10070000");

        if (foundBanks.isEmpty() == false) {
            BankData bank = new BankDataMapper().mapFromBankInfo(foundBanks.get(0));
            // set your customer data (customerId = Kontonummer in most cases, pin = online banking pin)
            CustomerData customer = new CustomerData("<customer_id>", "<pin>");
            customer.setSelectedTanProcedure(new TanProcedure("", Sicherheitsfunktion.PIN_TAN_911, TanProcedureType.ChipTanOptisch));

            FinTsClientCallback callback = new FinTsClientCallback() {
                @Nullable
                @Override
                public TanProcedure askUserForTanProcedure(@NotNull List<? extends TanProcedure> supportedTanProcedures) {
                    // TODO: if entering TAN is required select your tan procedure here
                    return supportedTanProcedures.get(0);
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

            // some banks support retrieving account transactions of last 90 days without TAN
            long ninetyDaysAgoMilliseconds = 90 * 24 * 60 * 60 * 1000L;
            Date ninetyDaysAgo = new Date(new Date().getTime() - ninetyDaysAgoMilliseconds);

            GetTransactionsResponse response = finTsClient.getTransactions(
                    new GetTransactionsParameter(true, ninetyDaysAgo), bank, customer);

            showResponse(response);
        }
    }

    private static void showResponse(GetTransactionsResponse response) {
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
                if (response.getException() != null) { // something severe occurred
                    System.out.println(response.getException().getMessage());
                }

                // error messages retrieved from bank (e.g. PIN is wrong, message contains errors, ...)
                for (String retrievedErrorMessage : response.getErrorsToShowToUser()) {
                    System.out.println(retrievedErrorMessage);
                }
            }
        }
    }

}
