package net.dankito.banking.ui.util

import net.dankito.utils.multiplatform.Freezer
import net.dankito.utils.multiplatform.ObjectHolder


open class FlickerCodeStepsCalculator {

    companion object {

        val bits = mutableMapOf<Char, Step>()

        init {
            /* bitfield: clock, bits 2^1, 2^2, 2^3, 2^4 */
            bits['0'] = Step(Bit.Low, Bit.Low, Bit.Low, Bit.Low, Bit.Low)
            bits['1'] = Step(Bit.Low, Bit.High, Bit.Low, Bit.Low, Bit.Low)
            bits['2'] = Step(Bit.Low, Bit.Low, Bit.High, Bit.Low, Bit.Low)
            bits['3'] = Step(Bit.Low, Bit.High, Bit.High, Bit.Low, Bit.Low)
            bits['4'] = Step(Bit.Low, Bit.Low, Bit.Low, Bit.High, Bit.Low)
            bits['5'] = Step(Bit.Low, Bit.High, Bit.Low, Bit.High, Bit.Low)
            bits['6'] = Step(Bit.Low, Bit.Low, Bit.High, Bit.High, Bit.Low)
            bits['7'] = Step(Bit.Low, Bit.High, Bit.High, Bit.High, Bit.Low)
            bits['8'] = Step(Bit.Low, Bit.Low, Bit.Low, Bit.Low, Bit.High)
            bits['9'] = Step(Bit.Low, Bit.High, Bit.Low, Bit.Low, Bit.High)
            bits['A'] = Step(Bit.Low, Bit.Low, Bit.High, Bit.Low, Bit.High)
            bits['B'] = Step(Bit.Low, Bit.High, Bit.High, Bit.Low, Bit.High)
            bits['C'] = Step(Bit.Low, Bit.Low, Bit.Low, Bit.High, Bit.High)
            bits['D'] = Step(Bit.Low, Bit.High, Bit.Low, Bit.High, Bit.High)
            bits['E'] = Step(Bit.Low, Bit.Low, Bit.High, Bit.High, Bit.High)
            bits['F'] = Step(Bit.Low, Bit.High, Bit.High, Bit.High, Bit.High)
        }

    }


    open fun calculateSteps(flickerCode: String): List<Step> {

        val halfbyteid = ObjectHolder(0)
        val clock = ObjectHolder(Bit.High)
        val bitarray = mutableListOf<Step>()


        /* prepend synchronization identifier */
        var code = "0FFF" + flickerCode
        if (code.length % 2 != 0) {
            code += "F"
        }

        for (i in 0 until code.length step 2) {
            bits[code[i + 1]]?.let { bitarray.add(it) }
            bits[code[i]]?.let { bitarray.add(it) }
        }

        val steps = mutableListOf<Step>()

        do {
            steps.add(calculateStep(halfbyteid, clock, bitarray))
        } while (halfbyteid.value > 0 || clock.value == Bit.Low)

        return Freezer.freeze(steps)
    }

    protected open fun calculateStep(halfbyteid: ObjectHolder<Int>, clock: ObjectHolder<Bit>, bitarray: List<Step>): Step {
        val step = Step(clock.value, bitarray[halfbyteid.value])


        clock.value = clock.value.invert()

        if (clock.value == Bit.High) {
            halfbyteid.value++

            if (halfbyteid.value >= bitarray.size) {
                halfbyteid.value = 0
            }

        }

        return step
    }

}