package net.dankito.banking.ui.android.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import net.dankito.banking.ui.android.R


open class FormSelectPeriod @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    protected lateinit var txtLabel: TextView

    protected lateinit var txtValue: TextView


    init {
        setupUi(context, attrs)
    }

    private fun setupUi(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_form_select_period, this, true)

        rootView.apply {
            txtLabel = findViewById(R.id.txtLabel)
            txtValue = findViewById(R.id.txtValue)

            txtLabel.gravity = Gravity.CENTER_VERTICAL
            txtValue.gravity = Gravity.CENTER_VERTICAL

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                txtLabel.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
                txtValue.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
            }

            this.isEnabled = false // TODO: undo as soon as selecting periods is implemented

            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FormLabelledValue,
                0, 0).apply {

                try {
                    txtLabel.text = getString(R.styleable.FormSelectPeriod_android_label)

                    val defaultValue = Int.MIN_VALUE
                    val configuredPeriod = getInteger(R.styleable.FormSelectPeriod_periodInMinutes, defaultValue)
                    periodInMinutes = if (configuredPeriod == defaultValue) null else configuredPeriod
                    displaySelectedPeriod()
                } finally {
                    recycle()
                }
            }
        }
    }


    protected open fun displaySelectedPeriod() {
        txtValue.text = getDisplayTextForSelectedPeriod()
    }

    protected open fun getDisplayTextForSelectedPeriod(): String {
        periodInMinutes?.let { periodInMinutes ->
            if (periodInMinutes > 0) {
                if (periodInMinutes < 60) {
                    return context.getString(R.string.minutes, periodInMinutes)
                }

                return context.getString(R.string.hours, (periodInMinutes / 60))
            }
        }

        return context.getString(R.string.never)
    }


    open var label: CharSequence
        get() = txtLabel.text
        set(value) {
            txtLabel.text = value
        }

    open var periodInMinutes: Int? = null
        set(value) {
            field = value
            displaySelectedPeriod()
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        alpha = if (enabled) 1f else 0.5f
    }

}