package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.response.ResponseParser

open class SupportedTanProceduresForUserFeedback(
    val supportedTanProcedures: List<Sicherheitsfunktion>,
    message: String
)
    : Feedback(ResponseParser.SupportedTanProceduresForUserResponseCode, message)