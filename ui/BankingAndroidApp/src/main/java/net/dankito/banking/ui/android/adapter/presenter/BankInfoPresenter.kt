package net.dankito.banking.ui.android.adapter.presenter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import kotlinx.coroutines.*
import net.dankito.banking.ui.android.adapter.BankListAdapter
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.fints.model.BankInfo


open class BankInfoPresenter(protected val presenter: BankingPresenter, context: Context) : RecyclerViewPresenter<BankInfo>(context) {

    protected val adapter = BankListAdapter { dispatchClick(it) }

    protected var lastSearchBanksJob: Job? = null


    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        return adapter
    }

    override fun onQuery(query: CharSequence?) {
        lastSearchBanksJob?.cancel()

        lastSearchBanksJob = GlobalScope.launch(Dispatchers.IO) {
            val filteredBanks = presenter.searchBanksByNameBankCodeOrCity(query?.toString())

            withContext(Dispatchers.Main) {
                adapter.items = filteredBanks
            }
        }
    }

}