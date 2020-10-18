package net.dankito.banking.ui.android.adapter

import net.dankito.banking.ui.model.tan.TanMethod


open class TanMethodAdapterItem(open val tanMethod: TanMethod, isSelectedTanMethod: Boolean)
    : CheckableValueAdapterItem(isSelectedTanMethod, tanMethod.displayName)