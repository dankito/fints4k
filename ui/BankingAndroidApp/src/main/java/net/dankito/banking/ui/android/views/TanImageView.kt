package net.dankito.banking.ui.android.views

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_tan_image.view.*
import kotlinx.android.synthetic.main.view_tan_image_size_controls.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.settings.ITanView
import net.dankito.banking.ui.model.settings.TanProcedureSettings
import net.dankito.banking.ui.model.tan.ImageTanChallenge


open class TanImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ITanView {

    companion object {
        const val ChangeSizeStepSizeDp = 10f
    }


    protected val MinHeight: Int

    protected val MaxHeight: Int

    protected lateinit var imgTanImageView: ImageView


    override var didTanProcedureSettingsChange: Boolean = false
        protected set

    override var tanProcedureSettings: TanProcedureSettings? = null
        protected set


    init {
        MinHeight = convertDpToPx(context, 150f).toInt()
        MaxHeight = convertDpToPx(context, 500f).toInt()

        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_tan_image, this, true)

        imgTanImageView = rootView.imgTanImageView

        rootView.btnDecreaseSize.setOnClickListener { decreaseSize() }
        rootView.btnIncreaseSize.setOnClickListener { increaseSize() }
    }


    open fun setImage(challenge: ImageTanChallenge, tanProcedureSettings: TanProcedureSettings?) {
        val decodedImage = challenge.image

        val bitmap = BitmapFactory.decodeByteArray(decodedImage.imageBytes, 0, decodedImage.imageBytes.size)
        rootView.imgTanImageView.setImageBitmap(bitmap)

        tanProcedureSettings?.let {
            setWidthAndHeight(it.width)
        }
    }

    open fun decreaseSize() {
        changeSizeBy(ChangeSizeStepSizeDp * -1)
    }

    open fun increaseSize() {
        changeSizeBy(ChangeSizeStepSizeDp)
    }

    protected open fun changeSizeBy(changeSizeBy: Float) {
        val params = imgTanImageView.layoutParams
        val newWidthAndHeight = params.height + convertDpToPx(context, changeSizeBy).toInt()

        setWidthAndHeight(newWidthAndHeight)
    }

    protected open fun setWidthAndHeight(newWidthAndHeight: Int) {
        if (newWidthAndHeight in MinHeight..MaxHeight) {
            val params = imgTanImageView.layoutParams

            params.height = newWidthAndHeight
            params.width = newWidthAndHeight

            imgTanImageView.layoutParams = params // TODO: needed?

            requestLayout()

            tanProcedureSettingsChanged(newWidthAndHeight)
        }
    }

    protected open fun tanProcedureSettingsChanged(newWidthAndHeight: Int) {
        tanProcedureSettings = TanProcedureSettings(newWidthAndHeight, newWidthAndHeight)

        didTanProcedureSettingsChange = true // we don't check if settings really changed, it's not that important
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