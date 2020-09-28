package net.dankito.banking.ui.android.adapter

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.mikepenz.fastadapter.items.AbstractItem
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.CheckableValueViewHolder


open class CheckableValueAdapterItem(open val isChecked: Boolean, open val text: String) : AbstractItem<CheckableValueViewHolder>() {

    constructor(isChecked: Boolean, context: Context, @StringRes textStringResourceId: Int) : this(isChecked, context.getString(textStringResourceId))


    override val type: Int
        get() = R.id.checkable_value_item_id

    override val layoutRes: Int
        get() = R.layout.list_item_checkable_value


    override fun getViewHolder(v: View): CheckableValueViewHolder {
        return CheckableValueViewHolder(v)
    }

}