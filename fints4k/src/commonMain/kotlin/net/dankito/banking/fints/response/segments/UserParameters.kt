package net.dankito.banking.fints.response.segments


open class UserParameters(
    val userIdentifier: String,

    /**
     * Antwortet ein Kreditinstitut auf das Kundensegment HKVVB und der UPD-Version=0 im Segment HIUPA ebenfalls mit
     * einer UPD-Version=0, so müssen im aktuellen Dialog diese übermittelten UPD verwendet werden; die UPD sind dann
     * nur für diesen Dialog gültig.
     */
    val updVersion: Int,
    val areListedJobsBlocked: Boolean,
    val username: String? = null,

    /**
     * Die innere Struktur dieses Parameterfeldes ist nicht weiter spezifiziert und kann von den Partnern bilateral
     * verwendet werden. Zur Selektion dieses neuen Datenelementes muss HKVVB (Verarbeitungsvorbereitung) mindestens
     * in der Segmentversion 3 gesendet werden.
     */
    val extension: String? = null,
    segmentString: String

)
    : ReceivedSegment(segmentString)