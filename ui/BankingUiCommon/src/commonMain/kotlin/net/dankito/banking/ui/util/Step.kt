package net.dankito.banking.ui.util


open class Step(
    val bit1: Bit,
    val bit2: Bit,
    val bit3: Bit,
    val bit4: Bit,
    val bit5: Bit
) {

    constructor(clockBit: Bit, step: Step) : this(clockBit, step.bit2, step.bit3, step.bit4, step.bit5)

}