package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.view_collapsible_text.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.util.StandardTextWatcher
import net.dankito.utils.android.extensions.asActivity
import java.util.*
import kotlin.concurrent.schedule


open class CollapsibleTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    companion object {

        const val CountDisplayedLinesWhenCollapsed = 3

    }

    protected var isCollapsed = true


    init {
        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_collapsible_text, this, true)

        rootView.apply {
            txtvwCollapsibleText.setOnClickListener { toggleIsCollapsed() }
            btnExpandCollapseTextView.setOnClickListener { toggleIsCollapsed() }

            txtvwCollapsibleText.addTextChangedListener(StandardTextWatcher {
                checkIfExpandCollapseButtonShouldBeDisplayed(context)
            })
        }
    }


    protected open fun toggleIsCollapsed() {
        if (isCollapsed) {
            txtvwCollapsibleText.maxLines = Int.MAX_VALUE
            btnExpandCollapseTextView.setImageResource(R.drawable.ic_baseline_expand_less_24)

            isCollapsed = false
        }
        else {
            txtvwCollapsibleText.maxLines = CountDisplayedLinesWhenCollapsed
            btnExpandCollapseTextView.setImageResource(R.drawable.ic_baseline_expand_more_24)

            isCollapsed = true
        }
    }

    protected open fun checkIfExpandCollapseButtonShouldBeDisplayed(context: Context) {
        Timer().schedule(500) { // wait some time till txtvwCollapsibleText is layout and lineCount is set
            context.asActivity()?.runOnUiThread {
                val showExpandButton = isCollapsed == false || txtvwCollapsibleText.lineCount > CountDisplayedLinesWhenCollapsed
                btnExpandCollapseTextView.visibility = if (showExpandButton) View.VISIBLE else View.GONE
            }
        }
    }

}