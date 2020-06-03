package net.dankito.banking.fints.response.segments


/**
 * Dieses Segment ist genau einmal für jedes Segment der Kundennachricht einzustellen. Hier sind sämtliche
 * Rückmeldungscodes aufzuführen, die sich auf das Kundensegment bzw. die zugehörigen Datenelemente und
 * Datenelementgruppen beziehen. Falls für das jeweilige Kundensegment keine Rückmeldungscodes erzeugt wurden,
 * kann das zugehörige Rückmeldesegment entfallen. Ist das jeweilige Kundensegment fehlerhaft, dann dürfen keine
 * Datensegmente (s.u.) rückgemeldet werden.
 */
open class SegmentFeedback(
    feedbacks: List<Feedback>,
    segmentString: String
)
    : MessageFeedback(feedbacks, segmentString)