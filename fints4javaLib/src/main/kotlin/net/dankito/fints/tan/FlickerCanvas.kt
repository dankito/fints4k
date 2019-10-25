package net.dankito.fints.tan

import org.slf4j.LoggerFactory


open class FlickerCanvas(var code: String) {

    companion object {
        private val log = LoggerFactory.getLogger(FlickerCanvas::class.java)
    }


    var halfbyteid = 0
    var clock = Bit.High
    var bitarray = mutableListOf<MutableList<Bit>>()

    val steps: List<Array<Bit>>


    fun reset() {
        halfbyteid = 0
        clock = Bit.High
    }

    init {
        val bits = mutableMapOf<Char, List<Bit>>()
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

        /* prepend synchronization identifier */
        code = "0FFF" + code

        for (i in 0 until code.length step 2) {
            bits[code[i + 1]]?.let { bitarray.add(mutableListOf(*it.toTypedArray())) }
            bits[code[i]]?.let { bitarray.add(mutableListOf(*it.toTypedArray())) }
        }

        val steps = mutableListOf<Array<Bit>>()

        do {
            steps.add(step())
        } while (halfbyteid > 0 || clock == Bit.Low)

        this.steps = steps
    }

    fun step(): Array<Bit> {
        bitarray[halfbyteid][0] = clock

        val stepBits = Array(5) { index -> bitarray[halfbyteid][index] }

        clock = clock.invert()

        if (clock == Bit.High) {
            halfbyteid++

            if (halfbyteid >= bitarray.size) {
                halfbyteid = 0
            }

        }

        return stepBits
    }

}