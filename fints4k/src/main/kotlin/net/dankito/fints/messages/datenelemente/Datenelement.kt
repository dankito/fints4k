package net.dankito.fints.messages.datenelemente

import net.dankito.fints.messages.Existenzstatus


abstract class Datenelement(existenzstatus: Existenzstatus): DatenelementBase(existenzstatus) {


    abstract val isValueSet: Boolean

    open val writeToOutput: Boolean
        get() = existenzstatus == Existenzstatus.Mandatory
                || existenzstatus == Existenzstatus.Optional && isValueSet


    @Throws(IllegalArgumentException::class)
    abstract fun validate()


    @Throws(IllegalArgumentException::class)
    protected fun throwValidationException(message: String) {
        throw IllegalArgumentException("Daten von ${javaClass.simpleName} sind ungültig: $message")
    }

}