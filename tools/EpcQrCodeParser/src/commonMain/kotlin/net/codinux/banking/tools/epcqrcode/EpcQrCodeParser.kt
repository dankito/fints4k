package net.codinux.banking.tools.epcqrcode


open class EpcQrCodeParser {

    open fun parseEpcQrCode(decodedQrCode: String): ParseEpcQrCodeResult {
        try {
            val lines = decodedQrCode.split("\n", "\r\n").dropLastWhile { isNotSet(it) }

            if (lines.size < 10 || lines.size > 12) {
                return createInvalidFormatResult(decodedQrCode, "A EPC-QR-Code consists of 10 to 12 lines, but passed string has ${lines.size} lines")
            }
            if (lines[0] != "BCD") {
                return createInvalidFormatResult(decodedQrCode, "A EPC-QR-Code's first lines has to be exactly 'BCD'")
            }

            return parseEpcQrAfterSuccessfulFormatCheck(decodedQrCode, lines)
        } catch (e: Exception) {
            return ParseEpcQrCodeResult(decodedQrCode, ParseEpcQrCodeResultCode.UnpredictedErrorOccurred, null, e.message)
        }
    }

    protected open fun parseEpcQrAfterSuccessfulFormatCheck(decodedQrCode: String, lines: List<String>): ParseEpcQrCodeResult {
        val validVersionCodes = EpcQrCodeVersion.values().map { it.code }
        if (lines[1].length != 3 || validVersionCodes.contains(lines[1]) == false) {
            return createInvalidFormatResult(decodedQrCode, "The second line has to be exactly one of these values: ${validVersionCodes.joinToString(", ")}")
        }

        val version = EpcQrCodeVersion.values().first { it.code == lines[1] }

        val codingCode = lines[2].toIntOrNull()
        if (lines[2].length != 1 || codingCode == null || codingCode < 1 || codingCode > 8) {
            return createInvalidFormatResult(decodedQrCode, "The third line has to be exactly one of these values: ${EpcQrCodeCharacterSet.values().map { it.code }.joinToString(", ")}")
        }

        val coding = EpcQrCodeCharacterSet.values().first { it.code == codingCode }

        if (lines[3].length != 3) {
            return createInvalidFormatResult(decodedQrCode, "The fourth line, ${lines[3]}, has to be exactly three characters long.")
        }

        if (version == EpcQrCodeVersion.Version1 && isNotSet(lines[4])) {
            return createInvalidFormatResult(decodedQrCode, "The BIC line 5 may only be omitted in EPC-QR-Code version 002")
        }
        if (lines[4].length != 8 && lines[4].length != 11 && (version == EpcQrCodeVersion.Version1 || isNotSet(lines[4]) == false)) {
            return createInvalidFormatResult(decodedQrCode, "The BIC line 5, ${lines[4]}, must be either 8 or 11 characters long.")
        }

        if (isNotSet(lines[5])) {
            return createInvalidFormatResult(decodedQrCode, "The receiver in line 6 may not be omitted.")
        }

        val receiver = parseWithCoding(lines[5], coding)
        if (receiver.length > 70) { // omit check for parsing
            return createInvalidFormatResult(decodedQrCode, "The receiver in line 6, ${lines[5]}, may not be longer than 70 characters.")
        }

        if (isNotSet(lines[6])) {
            return createInvalidFormatResult(decodedQrCode, "The IBAN in line 7 may not be omitted.")
        }
//        if (lines[6].length > 34) { // omit check for parsing
//            return createInvalidFormatResult("The IBAN in line 7, ${lines[6]}, may not be longer than 70 characters.")
//        }

        var currencyCode: String? = null
        var amount: Double? = null
        val currencyCodeAndAmount = lines[7]
        if (currencyCodeAndAmount.length > 3) {
            // TODO: check if the first three characters are letter
//            if (currencyCodeAndAmount.length > 15) {
//                return createInvalidFormatResult("The eighth line has to start with three upper case letters currency code and amount may not have more than 12 digits (including a dot as decimal separator).")
//            }

            currencyCode = currencyCodeAndAmount.substring(0, 3)
            amount = currencyCodeAndAmount.substring(3).toDouble()
        }

//        if (lines[8].length > 4) { // omit check for parsing
//            return createInvalidFormatResult("The purpose code in line 9, ${lines[8]}, may not be longer than 4 characters.")
//        }

//        if (lines[9].length > 35) { // omit check for parsing
//            return createInvalidFormatResult("The reconciliation reference in line 10, ${lines[9]}, may not be longer than 35 characters.")
//        }

        val reconciliationText = if (lines.size < 11) null else parseToNullableString(lines[10])?.let { parseWithCoding(it, coding) }
//        if ((reconciliationText?.length ?: 0) > 140) { // omit check for parsing
//            return createInvalidFormatResult("The reconciliation text in line 11, ${lines[10]}, may not be longer than 140 characters.")
//        }

        val displayText = if (lines.size < 12) null else lines[11]
//        if (displayText != null && displayText.length > 70) { // omit check for parsing
//            return createInvalidFormatResult("The display text in line 12, ${displayText}, may not be longer than 70 characters.")
//        }

        return ParseEpcQrCodeResult(
            decodedQrCode, ParseEpcQrCodeResultCode.Success, EpcQrCode(
                lines[0], version, coding, lines[3], parseToNullableString(lines[4]), lines[5], lines[6],
                currencyCode, amount, parseToNullableString(lines[8]), parseToNullableString(lines[9]), reconciliationText, displayText
            )
            , null
        )
    }

    protected open fun parseWithCoding(line: String, coding: EpcQrCodeCharacterSet): String {
        return line // TODO: does encoding work out of the box? // TODO: are there any encodings with more than one byte per characters allowed per specification
    }

    protected open fun parseToNullableString(lines: String): String? {
        return if (isNotSet(lines)) null else lines
    }

    protected open fun isNotSet(line: String): Boolean {
        return line.isBlank()
    }

    protected open fun createInvalidFormatResult(decodedQrCode: String, error: String): ParseEpcQrCodeResult {
        return ParseEpcQrCodeResult(decodedQrCode, ParseEpcQrCodeResultCode.NotAValidEpcQrCode, null, error)
    }

}