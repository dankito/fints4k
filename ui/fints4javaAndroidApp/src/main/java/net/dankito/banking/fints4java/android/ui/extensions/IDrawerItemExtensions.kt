package net.dankito.banking.fints4java.android.ui.extensions

import android.content.Context
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.iconics.withIcon
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem
import com.mikepenz.materialdrawer.model.BaseViewHolder
import com.mikepenz.materialdrawer.model.interfaces.withIconColor
import net.dankito.utils.android.extensions.createColorStateList


fun <T, VH : BaseViewHolder> BaseDescribeableDrawerItem<T, VH>.withIcon(context: Context, icon: IIcon, iconColorId: Int)
        : BaseDescribeableDrawerItem<T, VH> {

    withIcon(icon)

    withIconColor(context.createColorStateList(iconColorId))

    return this
}