package net.dankito.banking.ui.android.adapter.presenter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import kotlinx.coroutines.*
import net.dankito.banking.ui.android.adapter.RecipientListAdapter
import net.dankito.banking.search.TransactionParty
import net.dankito.banking.ui.android.extensions.addHorizontalItemDivider
import net.dankito.banking.ui.presenter.BankingPresenter


open class RecipientPresenter(protected val bankingPresenter: BankingPresenter, context: Context) : RecyclerViewPresenter<TransactionParty>(context) {

    protected val adapter = RecipientListAdapter { dispatchClick(it) }

    protected var lastSearchRecipientJob: Job? = null


    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        recyclerView?.addHorizontalItemDivider()

        return adapter
    }

    override fun onQuery(query: CharSequence?) {
        lastSearchRecipientJob?.cancel()

        lastSearchRecipientJob = GlobalScope.launch(Dispatchers.IO) {
            val potentialRecipients = bankingPresenter.findRecipientsForName(query?.toString() ?: "")

            withContext(Dispatchers.Main) {
                adapter.items = potentialRecipients
            }
        }
    }

}