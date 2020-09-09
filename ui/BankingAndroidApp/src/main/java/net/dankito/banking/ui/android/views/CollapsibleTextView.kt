package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import kotlinx.android.synthetic.main.view_collapsible_text.view.*
import net.dankito.banking.ui.android.R


open class CollapsibleTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    protected var isCollapsed = true


    init {
        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_collapsible_text, this, true)

        rootView.txtvwCollapsibleText.setOnClickListener { toggleIsCollapsed() }
        rootView.btnExpandCollapseTextView.setOnClickListener { toggleIsCollapsed() }
    }


    protected open fun toggleIsCollapsed() {
        if (isCollapsed) {
            txtvwCollapsibleText.maxLines = Int.MAX_VALUE
            btnExpandCollapseTextView.setImageResource(R.drawable.ic_baseline_expand_less_24)

            isCollapsed = false
        }
        else {
            txtvwCollapsibleText.maxLines = 3
            btnExpandCollapseTextView.setImageResource(R.drawable.ic_baseline_expand_more_24)

            isCollapsed = true
        }
    }

}