package net.dankito.fints.response.segments

import net.dankito.fints.response.ResponseParser


open class AufsetzpunktFeedback(
    val aufsetzpunkt: String,
    message: String
)
    : Feedback(ResponseParser.AufsetzpunktResponseCode, message)