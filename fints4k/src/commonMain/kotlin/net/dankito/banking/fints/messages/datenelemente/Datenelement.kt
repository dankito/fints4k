package net.dankito.banking.fints.messages.datenelemente

import net.dankito.banking.fints.messages.Existenzstatus


abstract class Datenelement(existenzstatus: Existenzstatus): DatenelementBase(existenzstatus) {


    abstract val isValueSet: Boolean

    open val writeToOutput: Boolean
        get() = existenzstatus == Existenzstatus.Mandatory
                || existenzstatus == Existenzstatus.Optional && isValueSet


    abstract fun validate()


    protected fun throwValidationException(message: String) {
        throw IllegalArgumentException("Daten von ${this::class.simpleName} sind ungültig: $message")
    }

}