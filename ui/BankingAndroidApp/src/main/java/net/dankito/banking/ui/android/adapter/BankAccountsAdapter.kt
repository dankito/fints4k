package net.dankito.banking.ui.android.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_bank_account.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.utils.android.ui.adapter.ListAdapter


open class BankAccountsAdapter(bankAccounts: List<TypedBankAccount>) : ListAdapter<TypedBankAccount>(bankAccounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val item = getItem(position)

        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = convertView ?: inflater?.inflate(R.layout.list_item_bank_account, parent, false)

        view?.let {
            view.txtBankAccountDisplayName.text = item.displayName

            setIcon(item, view.imgBankIcon)
        }

        return view
    }

    protected open fun setIcon(bankAccount: TypedBankAccount, imgBankIcon: ImageView) {
        try {
            val iconUrl = bankAccount.customer.iconUrl
            imgBankIcon.visibility = if (iconUrl == null) View.GONE else View.VISIBLE
            imgBankIcon.setImageURI(Uri.parse(iconUrl))
        } catch (e: Exception) {
            imgBankIcon.visibility = View.GONE
        }
    }

}