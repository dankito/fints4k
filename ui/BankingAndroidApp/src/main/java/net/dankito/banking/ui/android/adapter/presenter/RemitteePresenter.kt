package net.dankito.banking.ui.android.adapter.presenter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import kotlinx.coroutines.*
import net.dankito.banking.ui.android.adapter.RemitteeListAdapter
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.android.extensions.addHorizontalItemDivider
import net.dankito.banking.ui.presenter.BankingPresenter


open class RemitteePresenter(protected val bankingPresenter: BankingPresenter, context: Context) : RecyclerViewPresenter<Remittee>(context) {

    protected val adapter = RemitteeListAdapter { dispatchClick(it) }

    protected var lastSearchRemitteeJob: Job? = null


    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        recyclerView?.addHorizontalItemDivider()

        return adapter
    }

    override fun onQuery(query: CharSequence?) {
        lastSearchRemitteeJob?.cancel()

        lastSearchRemitteeJob = GlobalScope.launch(Dispatchers.IO) {
            val potentialRemittees = bankingPresenter.findRemitteesForName(query?.toString() ?: "")

            withContext(Dispatchers.Main) {
                adapter.items = potentialRemittees
            }
        }
    }

}