package net.dankito.banking.fints4java.android.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_tan_image.view.*
import kotlinx.android.synthetic.main.view_tan_image_size_controls.view.*
import net.dankito.banking.fints4java.android.R


open class TanImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        const val ChangeSizeStepSizeDp = 10f
    }


    protected val MinHeight: Int

    protected val MaxHeight: Int

    protected lateinit var imgTanImageView: ImageView


    init {
        MinHeight = convertDpToPx(context, 150f).toInt()
        MaxHeight = convertDpToPx(context, 500f).toInt()

        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_tan_image, this, true)

        imgTanImageView = rootView.imgTanImageView

        rootView.btnIncreaseSize.setOnClickListener { increaseSize() }
        rootView.btnDecreaseSize.setOnClickListener { decreaseSize() }
    }


    open fun increaseSize() {
        changeSizeBy(ChangeSizeStepSizeDp)
    }

    open fun decreaseSize() {
        changeSizeBy(ChangeSizeStepSizeDp * -1)
    }

    protected open fun changeSizeBy(changeSizeBy: Float) {
        val params = imgTanImageView.layoutParams
        val newWidthAndHeight = params.height + convertDpToPx(context, changeSizeBy).toInt()

        if (newWidthAndHeight in MinHeight..MaxHeight) {
            params.height = newWidthAndHeight
            params.width = newWidthAndHeight

            imgTanImageView.layoutParams = params // TODO: needed?

            requestLayout()
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    open fun convertDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

}