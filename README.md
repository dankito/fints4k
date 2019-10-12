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
  compile 'net.dankito.banking:fints4java:0.1'
}
```

Maven:
```
<dependency>
   <groupId>net.dankito.banking</groupId>
   <artifactId>fints4java</artifactId>
   <version>0.1</version>
</dependency>
```


## Usage

Get your bank's FinTS server address and BIC from this file:

```java
FinTsClient client = new FinTsClient();

```

## Logging

fints4java uses slf4j as logging facade.

So you can use any logger that supports slf4j, like Logback and log4j, to configure and get fints4java's log output.

## License
tbd.

In short: Non commercial projects can use it absolutely for free, commercial projects have to pay.