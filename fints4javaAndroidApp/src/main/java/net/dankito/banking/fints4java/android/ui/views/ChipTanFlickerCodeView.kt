package net.dankito.banking.fints4java.android.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_flicker_code.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.util.FlickercodeAnimator
import net.dankito.fints.tan.Bit
import net.dankito.fints.tan.Flickercode
import net.dankito.utils.android.extensions.asActivity


open class ChipTanFlickerCodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        const val FrequencyStepSize = 2
        const val MinFrequency = 2
        const val MaxFrequency = 40
    }


    protected lateinit var stripe1: ChipTanFlickerCodeStripeView
    protected lateinit var stripe2: ChipTanFlickerCodeStripeView
    protected lateinit var stripe3: ChipTanFlickerCodeStripeView
    protected lateinit var stripe4: ChipTanFlickerCodeStripeView
    protected lateinit var stripe5: ChipTanFlickerCodeStripeView

    protected val animator = FlickercodeAnimator()


    protected var currentWidth = 264f

    protected var currentFrequency = 30


    init {
        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_flicker_code, this, true)

        rootView.btnIncreaseSize.setOnClickListener { increaseSize() }
        rootView.btnDecreaseSize.setOnClickListener { decreaseSize() }

        rootView.btnIncreaseSpeed.setOnClickListener { increaseFrequency() }
        rootView.btnDecreaseSpeed.setOnClickListener { decreaseFrequency() }

        stripe1 = rootView.findViewById(R.id.flickerCodeStripe1)
        stripe2 = rootView.findViewById(R.id.flickerCodeStripe2)
        stripe3 = rootView.findViewById(R.id.flickerCodeStripe3)
        stripe4 = rootView.findViewById(R.id.flickerCodeStripe4)
        stripe5 = rootView.findViewById(R.id.flickerCodeStripe5)
    }



    override fun onDetachedFromWindow() {
        animator.stop()

        super.onDetachedFromWindow()
    }



    open fun increaseSize() {
        currentWidth += 10

        setWidth(context)
    }

    open fun decreaseSize() {
        currentWidth -= 10

        setWidth(context)
    }

    protected open fun setWidth(context: Context) {
        // TODO
//        val params = stripesView.layoutParams
////        params.width = convertDpToPx(context, currentWidth).toInt()
//        params.width = LayoutParams.WRAP_CONTENT
//        params.height = LayoutParams.WRAP_CONTENT // TODO: needed?
//        (params as? LinearLayoutCompat.LayoutParams)?.gravity = Gravity.CENTER_HORIZONTAL
//        stripesView.layoutParams = params
//
//        stripesView.requestLayout()
    }


    open fun increaseFrequency() {
        if (currentFrequency + FrequencyStepSize <= MaxFrequency) {
            currentFrequency += FrequencyStepSize

            setFrequency(currentFrequency)
        }
    }

    open fun decreaseFrequency() {
        if (currentFrequency - FrequencyStepSize >= MinFrequency) {
            currentFrequency -= FrequencyStepSize

            setFrequency(currentFrequency)
        }
    }

    open fun setFrequency(frequency: Int) {
        animator.setFrequency(frequency)
    }


    open fun setCode(flickercode: Flickercode) {
        animator.stop()

        setFrequency(currentFrequency)

        animator.animateFlickercode(flickercode) { step ->
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