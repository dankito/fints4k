package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView


open class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    companion object {
        const val DisableMaxHeight = -1
    }


    open var maxHeightInPixel = DisableMaxHeight
        protected set


    open fun setMaxHeightInDp(maxHeightInDp: Int) {
        val density = resources.displayMetrics.density
        maxHeightInPixel = (maxHeightInDp * density).toInt()
    }

    open fun disableMaxHeight() {
        maxHeightInPixel = DisableMaxHeight
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeightInPixel >= 0) {
            val adjustedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightInPixel, MeasureSpec.AT_MOST)

            super.onMeasure(widthMeasureSpec, adjustedHeightMeasureSpec)
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}