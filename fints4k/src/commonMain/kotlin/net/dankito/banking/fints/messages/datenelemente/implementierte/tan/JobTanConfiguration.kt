package net.dankito.banking.fints.messages.datenelemente.implementierte.tan


open class JobTanConfiguration(
    val segmentId: String,
    val tanRequired: Boolean
) {


    internal constructor() : this("", false) // for object deserializers


    override fun toString(): String {
        return "$segmentId requires TAN? $tanRequired"
    }

}