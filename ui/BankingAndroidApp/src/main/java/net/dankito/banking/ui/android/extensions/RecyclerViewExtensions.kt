package net.dankito.banking.ui.android.extensions

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView


fun RecyclerView.addHorizontalItemDivider() {
    this.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}