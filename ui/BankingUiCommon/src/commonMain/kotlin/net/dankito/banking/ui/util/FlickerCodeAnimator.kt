package net.dankito.banking.ui.util

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


    protected var currentFrequency: Int = DefaultFrequency

    protected var currentStepIndex = 0

    @Volatile
    protected var isPaused = false

//    protected var calculateAnimationThread: Thread? = null



    @JvmOverloads
    open fun animateFlickerCode(flickerCode: FlickerCode, frequency: Int = DefaultFrequency, showStep: (Array<Bit>) -> Unit) {
        currentFrequency = frequency
        currentStepIndex = 0
        val steps = FlickerCanvas(flickerCode.parsedDataSet).steps

        stop() // stop may still running previous animation

//        calculateAnimationThread = Thread({ calculateAnimation(steps, showStep) }, "CalculateFlickerCodeAnimation")
//
//        calculateAnimationThread?.start()
    }

//    protected open fun calculateAnimation(steps: List<Array<Bit>>, showStep: (Array<Bit>) -> Unit) {
//        while (Thread.currentThread().isInterrupted == false) {
//            if (isPaused == false) {
//                val nextStep = steps[currentStepIndex]
//
//                showStep(nextStep)
//
//                currentStepIndex++
//                if (currentStepIndex >= steps.size) {
//                    currentStepIndex = 0 // all steps shown, start again from beginning
//                }
//            }
//
//            try {
//                TimeUnit.MILLISECONDS.sleep(1000L / currentFrequency)
//            } catch (ignored: Exception) {
//                Thread.currentThread().interrupt()
//            }
//        }
//    }

    open fun pause() {
        this.isPaused = true
    }

    open fun resume() {
        this.isPaused = false
    }

    open fun stop() {
        try {
//            if (calculateAnimationThread?.isInterrupted == false) {
//                calculateAnimationThread?.interrupt()
//                calculateAnimationThread?.join(500)
//
//                calculateAnimationThread = null
//            }
        } catch (e: Exception) {
            log.warn(e) { "Could not stop calculateAnimationThread" }
        }
    }


    open fun setFrequency(frequency: Int) {
        if (frequency in MinFrequency..MaxFrequency) {
            currentFrequency = frequency
        }
    }

}