# EPC QR code Parser

The [EPC QR code](https://en.wikipedia.org/wiki/EPC_QR_code), marketed as GiroCode, Scan2Pay or Zahlen mit Code amongst others, is a QR code
defined by the European Payments Council which contains all data to initiate a SEPA credit transfer.

This library is a multi platform implementation to extract the credit transfer data from decoded QR code.
(So it does not implement decoding the QR code itself, but extracting the data from decoded QR code text.)