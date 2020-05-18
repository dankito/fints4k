package net.dankito.banking.fints4java.android.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import net.dankito.banking.fints.tan.Bit


open class ChipTanFlickerCodeStripeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    companion object {
        val White = Color.WHITE
        val Black = Color.BLACK
    }


    protected var currentColor = 0


    init {
        drawWhite()
    }


    open fun showStripe(showStripe: Bit) {
        if (showStripe == Bit.High) {
            drawWhite()
        }
        else {
            drawBlack()
        }
    }

    open fun drawWhite() {
        drawInColor(White)
    }

    open fun drawBlack() {
        drawInColor(Black)
    }

    open fun drawInColor(color: Int) {
        if (color != currentColor) {
            setBackgroundColor(color)

            currentColor = color

            invalidate()
        }
    }

}