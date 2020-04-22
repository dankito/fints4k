package net.dankito.banking.fints4java.android.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.iconics.IconicsImageHolder
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem
import com.mikepenz.materialdrawer.model.BaseViewHolder
import net.dankito.banking.fints4java.android.R
import net.dankito.utils.android.extensions.createColorStateList


open class SecondaryIconDrawerItem<Item : SecondaryIconDrawerItem<Item>> : BaseDescribeableDrawerItem<Item, SecondaryIconDrawerItem.ViewHolder>() {

    var secondaryIcon: ImageHolder? = null

    var secondaryIconColor: ColorStateList? = null

    var onSecondaryIconClicked: (() -> Unit)? = null


    override val type: Int
        get() = R.id.material_drawer_item_secondary_icon

    override val layoutRes: Int
        @LayoutRes
        get() = R.layout.material_drawer_item_secondary_icon


    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        bindViewHelper(holder)

        if (secondaryIcon == null) {
            holder.btnSecondaryIcon.visibility = View.GONE
        }
        else {
            val context = holder.itemView.context
            val secondaryIconColor = this.secondaryIconColor ?: getIconColor(context)
            val secondaryIcon = ImageHolder.decideIcon(secondaryIcon, context, secondaryIconColor, isIconTinted, 1)

            holder.btnSecondaryIcon.setImageDrawable(secondaryIcon)

            holder.btnSecondaryIcon.setOnClickListener { onSecondaryIconClicked?.invoke() }

            holder.btnSecondaryIcon.visibility = View.VISIBLE
        }

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, holder.itemView)
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    open class ViewHolder(view: View) : BaseViewHolder(view) {
        internal val btnSecondaryIcon = view.findViewById<ImageView>(R.id.btnSecondaryIcon)
    }


    open fun withSecondaryIconColor(iconColor: ColorStateList): Item {
        this.secondaryIconColor = iconColor
        return this as Item
    }

    open fun withSecondaryIconColor(context: Context, iconColorResId: Int): Item {
        return withSecondaryIconColor(context.createColorStateList(iconColorResId))
    }

    
    open fun withSecondaryIcon(icon: Drawable?): Item {
        this.secondaryIcon = ImageHolder(icon)
        return this as Item
    }

    open fun withSecondaryIcon(icon: Bitmap): Item {
        this.secondaryIcon = ImageHolder(icon)
        return this as Item
    }

    open fun withSecondaryIcon(@DrawableRes imageRes: Int): Item {
        this.secondaryIcon = ImageHolder(imageRes)
        return this as Item
    }

    open fun withSecondaryIcon(url: String): Item {
        this.secondaryIcon = ImageHolder(url)
        return this as Item
    }

    open fun withSecondaryIcon(uri: Uri): Item {
        this.secondaryIcon = ImageHolder(uri)
        return this as Item
    }

    open fun withSecondaryIcon(icon: ImageHolder?): Item {
        this.secondaryIcon = icon
        return this as Item
    }

    open fun withSecondaryIcon(icon: IIcon): Item {
        this.secondaryIcon = IconicsImageHolder(icon)
        return this as Item
    }


    open fun withOnSecondaryIconClickedListener(clickListener: () -> Unit): Item {
        this.onSecondaryIconClicked = clickListener
        return this as Item
    }

}