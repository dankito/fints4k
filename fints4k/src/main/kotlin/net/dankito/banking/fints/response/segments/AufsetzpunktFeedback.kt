package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.response.ResponseParser


open class AufsetzpunktFeedback(
    val aufsetzpunkt: String,
    message: String
)
    : Feedback(ResponseParser.AufsetzpunktResponseCode, message)