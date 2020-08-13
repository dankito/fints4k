package net.dankito.banking.ui.util

import kotlinx.coroutines.*
import net.dankito.banking.ui.model.tan.FlickerCode
import net.dankito.banking.fints.tan.Bit
import net.dankito.banking.fints.tan.FlickerCanvas
import net.dankito.utils.multiplatform.log.LoggerFactory
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile


open class FlickerCodeAnimator {

    companion object {
        const val MinFrequency = 2
        const val MaxFrequency = 40
        const val DefaultFrequency = 20

        private val log = LoggerFactory.getLogger(FlickerCodeAnimator::class)
    }


    @Volatile
    protected var currentFrequency: Int = DefaultFrequency

    @Volatile
    protected var isPaused = false

    protected var animationJob: Job? = null



    open fun animateFlickerCode(flickerCode: FlickerCode, showStep: (Array<Bit>) -> Unit) {
        animateFlickerCode(flickerCode, DefaultFrequency, showStep)
    }

    open fun animateFlickerCode(flickerCode: FlickerCode, frequency: Int, showStep: (Array<Bit>) -> Unit) {
        stop() // stop may still running previous animation

        currentFrequency = frequency

        animationJob = GlobalScope.launch(Dispatchers.Default) {
            val steps = FlickerCanvas(flickerCode.parsedDataSet).steps

            calculateAnimation(steps, showStep)
        }
    }

    protected open suspend fun calculateAnimation(steps: List<Array<Bit>>, showStep: (Array<Bit>) -> Unit) {
        var currentStepIndex = 0

        while (true) {
            if (isPaused == false) {
                val nextStep = steps[currentStepIndex]

                withContext(Dispatchers.Main) {
                    showStep(nextStep)
                }

                currentStepIndex++
                if (currentStepIndex >= steps.size) {
                    currentStepIndex = 0 // all steps shown, start again from beginning
                }
            }

            delay(1000L / currentFrequency)
        }
    }

    open fun pause() {
        this.isPaused = true
    }

    open fun resume() {
        this.isPaused = false
    }

    open fun stop() {
        try {
            animationJob?.cancel()

            animationJob = null
        } catch (e: Exception) {
            log.warn(e) { "Could not stop animation job" }
        }
    }


    open fun setFrequency(frequency: Int) {
        if (frequency in MinFrequency..MaxFrequency) {
            currentFrequency = frequency
        }
    }

}