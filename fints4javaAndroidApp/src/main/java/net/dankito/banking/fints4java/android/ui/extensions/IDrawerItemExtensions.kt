package net.dankito.banking.fints4java.android.ui.extensions

import android.content.Context
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.iconics.withIcon
import com.mikepenz.materialdrawer.model.AbstractBadgeableDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.withIconColor
import net.dankito.utils.android.extensions.createColorStateList


fun <Item : AbstractBadgeableDrawerItem<Item>> AbstractBadgeableDrawerItem<Item>.withIcon(
    context: Context, icon: IIcon, iconColorId: Int): AbstractBadgeableDrawerItem<Item> {

    withIcon(icon)

    withIconColor(context.createColorStateList(iconColorId))

    return this
}