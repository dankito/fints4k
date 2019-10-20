package net.dankito.banking.fints4java.android.ui.adapter.filter

import android.widget.Filter
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.BankInfo


open class BankInfoFilter(protected val bankFinder: BankFinder,
                          protected val publishResultsCallback: (List<BankInfo>) -> Unit) : Filter() {


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()

        constraint?.let {
            results.values = bankFinder.findBankByNameBankCodeOrCity(it.toString())
        }
            ?: run {
                results.values = bankFinder.getBankList()
            }

        results.count = (results.values as List<*>).size

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        publishResultsCallback(results.values as List<BankInfo>)
    }

}