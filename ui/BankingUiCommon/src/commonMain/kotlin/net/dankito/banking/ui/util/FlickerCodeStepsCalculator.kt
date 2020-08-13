package net.dankito.banking.ui.util

import net.dankito.utils.multiplatform.ObjectHolder


open class FlickerCodeStepsCalculator {

    companion object {

        val bits = mutableMapOf<Char, List<Bit>>()

        init {
            /* bitfield: clock, bits 2^1, 2^2, 2^3, 2^4 */
            bits['0'] = listOf(Bit.Low, Bit.Low, Bit.Low, Bit.Low, Bit.Low)
            bits['1'] = listOf(Bit.Low, Bit.High, Bit.Low, Bit.Low, Bit.Low)
            bits['2'] = listOf(Bit.Low, Bit.Low, Bit.High, Bit.Low, Bit.Low)
            bits['3'] = listOf(Bit.Low, Bit.High, Bit.High, Bit.Low, Bit.Low)
            bits['4'] = listOf(Bit.Low, Bit.Low, Bit.Low, Bit.High, Bit.Low)
            bits['5'] = listOf(Bit.Low, Bit.High, Bit.Low, Bit.High, Bit.Low)
            bits['6'] = listOf(Bit.Low, Bit.Low, Bit.High, Bit.High, Bit.Low)
            bits['7'] = listOf(Bit.Low, Bit.High, Bit.High, Bit.High, Bit.Low)
            bits['8'] = listOf(Bit.Low, Bit.Low, Bit.Low, Bit.Low, Bit.High)
            bits['9'] = listOf(Bit.Low, Bit.High, Bit.Low, Bit.Low, Bit.High)
            bits['A'] = listOf(Bit.Low, Bit.Low, Bit.High, Bit.Low, Bit.High)
            bits['B'] = listOf(Bit.Low, Bit.High, Bit.High, Bit.Low, Bit.High)
            bits['C'] = listOf(Bit.Low, Bit.Low, Bit.Low, Bit.High, Bit.High)
            bits['D'] = listOf(Bit.Low, Bit.High, Bit.Low, Bit.High, Bit.High)
            bits['E'] = listOf(Bit.Low, Bit.Low, Bit.High, Bit.High, Bit.High)
            bits['F'] = listOf(Bit.Low, Bit.High, Bit.High, Bit.High, Bit.High)
        }

    }


    open fun calculateSteps(flickerCode: String): List<Array<Bit>> {

        val halfbyteid = ObjectHolder(0)
        val clock = ObjectHolder(Bit.High)
        val bitarray = mutableListOf<MutableList<Bit>>()


        /* prepend synchronization identifier */
        var code = "0FFF" + flickerCode
        if (code.length % 2 != 0) {
            code += "F"
        }

        for (i in 0 until code.length step 2) {
            bits[code[i + 1]]?.let { bitarray.add(mutableListOf(*it.toTypedArray())) }
            bits[code[i]]?.let { bitarray.add(mutableListOf(*it.toTypedArray())) }
        }

        val steps = mutableListOf<Array<Bit>>()

        do {
            steps.add(calculateStep(halfbyteid, clock, bitarray))
        } while (halfbyteid.value > 0 || clock.value == Bit.Low)

        return steps
    }

    protected open fun calculateStep(halfbyteid: ObjectHolder<Int>, clock: ObjectHolder<Bit>, bitarray: MutableList<MutableList<Bit>>): Array<Bit> {
        bitarray[halfbyteid.value][0] = clock.value

        val stepBits = Array(5) { index -> bitarray[halfbyteid.value][index] }

        clock.value = clock.value.invert()

        if (clock.value == Bit.High) {
            halfbyteid.value++

            if (halfbyteid.value >= bitarray.size) {
                halfbyteid.value = 0
            }

        }

        return stepBits
    }

}