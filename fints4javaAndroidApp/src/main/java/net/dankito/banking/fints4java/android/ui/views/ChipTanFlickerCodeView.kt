package net.dankito.banking.fints4java.android.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_flicker_code.view.*
import kotlinx.android.synthetic.main.view_tan_image_size_controls.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.ui.model.tan.FlickerCode
import net.dankito.banking.ui.util.FlickerCodeAnimator
import net.dankito.fints.tan.Bit
import net.dankito.utils.android.extensions.asActivity


open class ChipTanFlickerCodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        const val FrequencyStepSize = 2
        const val MinFrequency = 2
        const val MaxFrequency = 40

        const val StripesHeightStepSize = 7
        const val StripesWidthStepSize = 2
        const val StripesRightMarginStepSize = 1
    }


    protected lateinit var stripe1: ChipTanFlickerCodeStripeView
    protected lateinit var stripe2: ChipTanFlickerCodeStripeView
    protected lateinit var stripe3: ChipTanFlickerCodeStripeView
    protected lateinit var stripe4: ChipTanFlickerCodeStripeView
    protected lateinit var stripe5: ChipTanFlickerCodeStripeView

    protected lateinit var allStripes: List<ChipTanFlickerCodeStripeView>

    protected lateinit var tanGeneratorLeftMarker: View
    protected lateinit var tanGeneratorRightMarker: View

    protected lateinit var btnPauseFlickerCode: ImageButton

    protected val animator = FlickerCodeAnimator()


    protected var stripesHeight = 360
    protected var stripesWidth = 120
    protected var stripesMarginRight = 30.0 // set back to 30

    protected var currentFrequency = 30

    protected var isFlickerCodePaused = false


    init {
        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_flicker_code, this, true)

        rootView.btnDecreaseSize.setOnClickListener { decreaseSize() }
        rootView.btnIncreaseSize.setOnClickListener { increaseSize() }

        rootView.btnDecreaseSpeed.setOnClickListener { decreaseFrequency() }
        rootView.btnIncreaseSpeed.setOnClickListener { increaseFrequency() }

        btnPauseFlickerCode = rootView.btnPauseFlickerCode
        btnPauseFlickerCode.setOnClickListener { togglePauseFlickerCode() }

        stripe1 = rootView.findViewById(R.id.flickerCodeStripe1)
        stripe2 = rootView.findViewById(R.id.flickerCodeStripe2)
        stripe3 = rootView.findViewById(R.id.flickerCodeStripe3)
        stripe4 = rootView.findViewById(R.id.flickerCodeStripe4)
        stripe5 = rootView.findViewById(R.id.flickerCodeStripe5)

        allStripes = listOf(stripe1, stripe2, stripe3, stripe4, stripe5)

        stripesHeight = stripe1.layoutParams.height
        stripesWidth = stripe1.layoutParams.width
        (stripe1.layoutParams as? MarginLayoutParams)?.let { stripesMarginRight = it.rightMargin.toDouble() }

        tanGeneratorLeftMarker = rootView.findViewById(R.id.tanGeneratorLeftMarker)
        tanGeneratorRightMarker = rootView.findViewById(R.id.tanGeneratorRightMarker)

        setMarkerPositionAfterStripesLayoutSet()
    }


    override fun onDetachedFromWindow() {
        animator.stop()

        super.onDetachedFromWindow()
    }



    open fun decreaseSize() {
        stripesHeight -= StripesHeightStepSize
        stripesWidth -= StripesWidthStepSize
        stripesMarginRight -= StripesRightMarginStepSize

        setWidth(context)
    }

    open fun increaseSize() {
        stripesHeight += StripesHeightStepSize
        stripesWidth += StripesWidthStepSize
        stripesMarginRight += StripesRightMarginStepSize

        setWidth(context)
    }

    protected open fun setWidth(context: Context) {
        allStripes.forEach { stripe ->
            val params = stripe.layoutParams
            params.height = stripesHeight
            params.width = stripesWidth

            (params as? MarginLayoutParams)?.let { marginParams ->
                if (marginParams.rightMargin > 0) { // don't set a margin right on fifth stripe
                    marginParams.rightMargin = stripesMarginRight.toInt()
                }
            }

            stripe.layoutParams = params
        }

        requestLayout()

        setMarkerPositionAfterStripesLayoutSet()
    }

    protected open fun setMarkerPositionAfterStripesLayoutSet() {
        postDelayed({ setMarkerPosition() }, 10L) // we need to wait till layout for stripes is applied before we can set marker positions correctly
    }

    protected open fun setMarkerPosition() {
        tanGeneratorLeftMarker.x = stripe1.x + (stripe1.width - tanGeneratorLeftMarker.layoutParams.width) / 2

        tanGeneratorRightMarker.x = stripe5.x + (stripe5.width - tanGeneratorRightMarker.layoutParams.width) / 2
    }


    open fun increaseFrequency() {
        if (currentFrequency + FrequencyStepSize <= MaxFrequency) {
            currentFrequency += FrequencyStepSize

            setFrequency(currentFrequency)
        }
    }

    open fun setFrequency(frequency: Int) {
        animator.setFrequency(frequency)
    }


    open fun togglePauseFlickerCode() {
        if (isFlickerCodePaused == false) {
            animator.pause()
            btnPauseFlickerCode.setImageResource(android.R.drawable.ic_media_play)
        }
        else {
            animator.resume()
            btnPauseFlickerCode.setImageResource(android.R.drawable.ic_media_pause)
        }

        isFlickerCodePaused = !!! isFlickerCodePaused
    }


    open fun setCode(flickerCode: FlickerCode) {
        animator.stop()

        setFrequency(currentFrequency)

        animator.animateFlickerCode(flickerCode) { step ->
            context.asActivity()?.runOnUiThread {
                showStepOnUiThread(step)
            }
        }
    }

    protected open fun showStepOnUiThread(step: Array<Bit>) {

        stripe1.showStripe(step[0])
        stripe2.showStripe(step[1])
        stripe3.showStripe(step[2])
        stripe4.showStripe(step[3])
        stripe5.showStripe(step[4])
    }

}