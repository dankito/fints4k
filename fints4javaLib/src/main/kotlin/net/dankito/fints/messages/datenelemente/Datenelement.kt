package net.dankito.fints.messages.datenelemente

import net.dankito.fints.messages.Existenzstatus


abstract class Datenelement(existenzstatus: Existenzstatus): DatenelementBase(existenzstatus) {


    @Throws(IllegalArgumentException::class)
    abstract fun validate()


    @Throws(IllegalArgumentException::class)
    protected fun throwValidationException(message: String) {
        throw IllegalArgumentException("Daten von ${javaClass.simpleName} sind ungültig: $message")
    }

}