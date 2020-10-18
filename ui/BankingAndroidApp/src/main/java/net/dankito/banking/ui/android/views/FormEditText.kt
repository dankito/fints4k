package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_form_edit_text.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.extensions.textString


open class FormEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    init {
        setupUi(context, attrs)
    }

    private fun setupUi(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_form_edit_text, this, true)

        rootView.apply {

            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FormEditText,
                0, 0).apply {

                try {
                    textInputLayout.hint = getString(R.styleable.FormEditText_android_hint)
                    if (getBoolean(R.styleable.FormEditText_showPasswordToggle, false)) {
                        textInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    }

                    textInputEditText.setText(getString(R.styleable.FormEditText_android_text))
                    textInputEditText.inputType = getInt(R.styleable.FormEditText_android_inputType, EditorInfo.TYPE_TEXT_VARIATION_NORMAL)
                    textInputEditText.setSelectAllOnFocus(getBoolean(R.styleable.FormEditText_android_selectAllOnFocus, false))
                } finally {
                    recycle()
                }
            }
        }
    }


    open var text: String
        get() = textInputEditText.textString
        set(value) = textInputEditText.setText(value)

    open val chars: CharArray
        get() = actualEditText.text.toList().toCharArray()

    open val actualEditText: EditText
        get() = textInputEditText

}