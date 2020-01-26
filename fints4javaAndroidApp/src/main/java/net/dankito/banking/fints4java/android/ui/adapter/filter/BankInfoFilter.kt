package net.dankito.banking.fints4java.android.ui.adapter.filter

import android.widget.Filter
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.fints.model.BankInfo


open class BankInfoFilter(protected val presenter: BankingPresenter,
                          protected val publishResultsCallback: (List<BankInfo>) -> Unit) : Filter() {


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val filteredBanks = presenter.searchBanksByNameBankCodeOrCity(constraint?.toString())

        val results = FilterResults()
        results.values = filteredBanks

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        publishResultsCallback(results.values as List<BankInfo>)
    }

}