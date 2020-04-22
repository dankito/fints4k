# fints4java

fints4java is an implementation of the FinTS 3.0 online banking protocol used by most German banks.

There's already a full functional FinTS/HBCI library for Java: [hbci4java](https://github.com/hbci4j/hbci4java).
So why did I take the trouble to write a new one?

- hbci4java does not run on Android.
- It's hard to configure (e.g. to get the account transactions from a particular day onwards you have to browse the source to find out that you have to add the key "startdate" to a Map and as value the date as a string with format "yyyy-MM-dd").
- It's hard to extend. I wanted to implement SEPA instant payments, but you would have to dive deep into the source and implement it there. There's absolutely no way to (quickly) add it from your source by extending the library.


## Features / Limitations
- Supports only FinTS 3.0 which is used by most banks (HBCI 2.x is obsolete, FinTS 4.x is only used by one bank according to offical bank list).
- Supports only PIN/TAN (used by most users), no signature cards.
- Supports only chipTAN.
- Supported operations:
    - Get account info
    - Get account transactions
    - Normal, scheduled and instant payment (SEPA) cash transfer

## Setup
Not uploaded to Maven Central yet, will do this the next few days!

Gradle:
```
dependencies {
  compile 'net.dankito.banking:fints4java:0.1.0'
}
```

Maven:
```
<dependency>
   <groupId>net.dankito.banking</groupId>
   <artifactId>fints4java</artifactId>
   <version>0.1.0</version>
</dependency>
```


## Usage

See e.g. [JavaShowcase](fints4javaLib/src/test/java/net/dankito/fints/java/JavaShowcase) or [FinTsClientTest](fints4javaLib/src/test/kotlin/net/dankito/fints/FinTsClientTest).

```java
    public static void main(String[] args) {
        BankFinder bankFinder = new BankFinder();

        // set your bank code (Bankleitzahl) here. Or create BankData manually. Required fields are:
        // bankCode, bankCountryCode (Germany = 280), finTs3ServerAddress and for bank transfers bic
        List<BankInfo> foundBanks = bankFinder.findBankByBankCode("10070000");

        if (foundBanks.isEmpty() == false) {
            BankData bank = new BankDataMapper().mapFromBankInfo(foundBanks.get(0));
            // set your customer data (customerId = Kontonummer in most cases, pin = online banking pin)
            CustomerData customer = new CustomerData("<customer_id>", "<pin>");
            customer.setSelectedTanProcedure(new TanProcedure("", Sicherheitsfunktion.PIN_TAN_911, TanProcedureType.ChipTan));

            FinTsClient finTsClient = new FinTsClient(new Java8Base64Service());

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

```

## Logging

fints4java uses slf4j as logging facade.

So you can use any logger that supports slf4j, like Logback and log4j, to configure and get fints4java's log output.

## License
tbd.

In short: Non commercial projects can use it absolutely for free, commercial projects have to pay.