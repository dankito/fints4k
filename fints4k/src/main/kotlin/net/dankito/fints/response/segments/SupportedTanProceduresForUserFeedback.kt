package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.response.ResponseParser

open class SupportedTanProceduresForUserFeedback(
    val supportedTanProcedures: List<Sicherheitsfunktion>,
    message: String
)
    : Feedback(ResponseParser.SupportedTanProceduresForUserResponseCode, message)