package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_flicker_code.view.*
import kotlinx.android.synthetic.main.view_tan_image_size_controls.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.tan.FlickerCode
import net.dankito.banking.ui.util.FlickerCodeAnimator
import net.dankito.banking.ui.model.settings.ITanView
import net.dankito.banking.ui.model.settings.TanMethodSettings
import net.dankito.banking.ui.util.Step


open class ChipTanFlickerCodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ITanView {

    companion object {
        const val FrequencyStepSize = 2
        const val MinFrequency = 2
        const val MaxFrequency = 40
        const val DefaultFrequency = 30

        const val StripesHeightStepSize = 7
        const val StripesWidthStepSize = 2
        const val SpaceBetweenStripesStepSize = 1
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
    protected var spaceBetweenStripes = 30

    protected var currentFrequency = DefaultFrequency

    protected var isFlickerCodePaused = false


    override var didTanMethodSettingsChange: Boolean = false
        protected set

    override var tanMethodSettings: TanMethodSettings? = null
        protected set


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
        (stripe1.layoutParams as? MarginLayoutParams)?.let { spaceBetweenStripes = it.rightMargin }

        tanGeneratorLeftMarker = rootView.findViewById(R.id.tanGeneratorLeftMarker)
        tanGeneratorRightMarker = rootView.findViewById(R.id.tanGeneratorRightMarker)

        setMarkerPositionAfterStripesLayoutSet()

        tanMethodSettings?.let {
            setSize(it.width, it.height, it.space)
            setFrequency(it.frequency)
        }
    }


    override fun onDetachedFromWindow() {
        animator.stop()

        super.onDetachedFromWindow()
    }



    open fun decreaseSize() {
        setSize(
            stripesWidth - StripesWidthStepSize,
            stripesHeight - StripesHeightStepSize,
            spaceBetweenStripes - SpaceBetweenStripesStepSize
        )
    }

    open fun increaseSize() {
        setSize(
            stripesWidth + StripesWidthStepSize,
            stripesHeight + StripesHeightStepSize,
            spaceBetweenStripes + SpaceBetweenStripesStepSize
        )
    }

    open fun setSize(width: Int, height: Int, spaceBetweenStripes: Int) {
        this.stripesWidth = width
        this.stripesHeight = height
        this.spaceBetweenStripes = spaceBetweenStripes

        applySize()
    }

    protected open fun applySize() {
        allStripes.forEach { stripe ->
            val params = stripe.layoutParams
            params.height = stripesHeight
            params.width = stripesWidth

            (params as? MarginLayoutParams)?.let { marginParams ->
                if (marginParams.rightMargin > 0) { // don't set a margin right on fifth stripe
                    marginParams.rightMargin = spaceBetweenStripes
                }
            }

            stripe.layoutParams = params
        }

        requestLayout()

        setMarkerPositionAfterStripesLayoutSet()

        tanMethodSettingsChanged()
    }

    protected open fun setMarkerPositionAfterStripesLayoutSet() {
        postDelayed({ setMarkerPosition() }, 10L) // we need to wait till layout for stripes is applied before we can set marker positions correctly
    }

    protected open fun setMarkerPosition() {
        tanGeneratorLeftMarker.x = stripe1.x + (stripe1.width - tanGeneratorLeftMarker.layoutParams.width) / 2

        tanGeneratorRightMarker.x = stripe5.x + (stripe5.width - tanGeneratorRightMarker.layoutParams.width) / 2
    }


    open fun decreaseFrequency() {
        if (currentFrequency - FrequencyStepSize >= MinFrequency) {
            setFrequency(currentFrequency - FrequencyStepSize)
        }
    }

    open fun increaseFrequency() {
        if (currentFrequency + FrequencyStepSize <= MaxFrequency) {
            setFrequency(currentFrequency + FrequencyStepSize)
        }
    }

    open fun setFrequency(frequency: Int) {
        currentFrequency = frequency

        animator.setFrequency(frequency)

        tanMethodSettingsChanged()
    }

    protected open fun tanMethodSettingsChanged() {
        tanMethodSettings = TanMethodSettings(stripesWidth, stripesHeight, spaceBetweenStripes, currentFrequency)

        didTanMethodSettingsChange = true // we don't check if settings really changed, it's not that important
    }


    open fun togglePauseFlickerCode() {
        if (isFlickerCodePaused == false) {
            animator.pause()
            btnPauseFlickerCode.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        else {
            animator.resume()
            btnPauseFlickerCode.setImageResource(R.drawable.ic_baseline_pause_24)
        }

        isFlickerCodePaused = !!! isFlickerCodePaused
    }


    open fun setCode(flickerCode: FlickerCode, tanMethodSettings: TanMethodSettings?) {
        animator.stop()

        tanMethodSettings?.let {
            setSize(it.width, it.height, it.space)
            setFrequency(it.frequency)
        }
        ?: run {
            setFrequency(DefaultFrequency)
        }

        this.tanMethodSettings = tanMethodSettings
        this.didTanMethodSettingsChange = false

        animator.animateFlickerCode(flickerCode) { step ->
            showStepOnUiThread(step)
        }
    }

    protected open fun showStepOnUiThread(step: Step) {

        stripe1.setStripeVisibility(step.bit1)
        stripe2.setStripeVisibility(step.bit2)
        stripe3.setStripeVisibility(step.bit3)
        stripe4.setStripeVisibility(step.bit4)
        stripe5.setStripeVisibility(step.bit5)
    }

}