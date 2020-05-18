package net.dankito.banking.ui.android.util

import android.text.Editable
import com.otaliastudios.autocomplete.AutocompleteCallback


open class StandardAutocompleteCallback<T>(
    protected val onPopupVisibilityChanged: ((shown: Boolean) -> Unit)? = null,
    protected val onPopupItemClicked: ((editable: Editable?, item: T) -> Boolean)? = null
) : AutocompleteCallback<T> {

    override fun onPopupItemClicked(editable: Editable?, item: T): Boolean {
        return onPopupItemClicked?.invoke(editable, item) ?: false
    }

    override fun onPopupVisibilityChanged(shown: Boolean) {
        onPopupVisibilityChanged?.invoke(shown)
    }

}