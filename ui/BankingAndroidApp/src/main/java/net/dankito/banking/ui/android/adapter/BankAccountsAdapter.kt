package net.dankito.banking.ui.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_bank_account.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.BankAccount
import net.dankito.utils.android.ui.adapter.ListAdapter


open class BankAccountsAdapter(bankAccounts: List<BankAccount>) : ListAdapter<BankAccount>(bankAccounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val item = getItem(position)

        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = convertView ?: inflater?.inflate(R.layout.list_item_bank_account, parent, false)

        view?.let {
            view.txtBankAccountDisplayName.text = item.displayNameIncludingBankName
        }

        return view
    }

}