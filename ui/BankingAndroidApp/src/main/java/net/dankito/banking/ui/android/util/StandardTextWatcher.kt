package net.dankito.banking.ui.android.util

import android.text.Editable
import android.text.TextWatcher


open class StandardTextWatcher(
    protected val beforeTextChanged: ((string: CharSequence?, start: Int, count: Int, after: Int) -> Unit)? = null,
    protected val onFullParameterizedTextChanged: ((string: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null,
    protected val afterTextChanged: ((editable: Editable?) -> Unit)? = null,
    protected val onTextChanged: ((string: CharSequence) -> Unit)? = null
) : TextWatcher {

    override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {
        beforeTextChanged?.invoke(string, start, count, after)
    }

    override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged?.invoke(string ?: "") // can string parameter ever be null?
        onFullParameterizedTextChanged?.invoke(string, start, before, count)
    }

    override fun afterTextChanged(editable: Editable?) {
        afterTextChanged?.invoke(editable)
    }

}