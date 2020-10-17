package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_bank_credentials_password.view.*
import kotlinx.android.synthetic.main.view_form_edit_text.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.extensions.textString


open class BankCredentialsPasswordView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    init {
        setupUi(context, attrs)
    }

    private fun setupUi(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_bank_credentials_password, this, true)
    }


    open var password: String
        get() = edtxtPassword.text
        set(value) {
            edtxtPassword.text = value
        }

    open val passwordBox: TextInputEditText
        get() = textInputEditText

    open var savePassword: Boolean
        get() = swtchSavePassword.isChecked
        set(value) {
            swtchSavePassword.isChecked = value
        }

}