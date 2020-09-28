package net.dankito.banking.ui.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_bank_account.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.extensions.setIcon
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.utils.android.ui.adapter.ListAdapter


open class BankAccountsAdapter(accounts: List<TypedBankAccount>) : ListAdapter<TypedBankAccount>(accounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val item = getItem(position)

        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = convertView ?: inflater?.inflate(R.layout.list_item_bank_account, parent, false)

        view?.let {
            view.txtBankAccountDisplayName.text = item.displayName

            view.imgBankIcon.setIcon(item.bank)
        }

        return view
    }

}