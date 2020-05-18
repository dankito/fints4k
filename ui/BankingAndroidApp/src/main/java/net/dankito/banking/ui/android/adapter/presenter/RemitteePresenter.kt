package net.dankito.banking.ui.android.adapter.presenter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import kotlinx.coroutines.*
import net.dankito.banking.ui.android.adapter.RemitteeListAdapter
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.Remittee
import net.dankito.utils.Stopwatch


open class RemitteePresenter(protected val remitteeSearcher: IRemitteeSearcher, context: Context) : RecyclerViewPresenter<Remittee>(context) {

    protected val adapter = RemitteeListAdapter { dispatchClick(it) }

    protected var lastSearchRemitteeJob: Job? = null


    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        return adapter
    }

    override fun onQuery(query: CharSequence?) {
        lastSearchRemitteeJob?.cancel()

        lastSearchRemitteeJob = GlobalScope.launch(Dispatchers.IO) {
            val potentialRemittees = Stopwatch.logDuration("findRemittees()") { remitteeSearcher.findRemittees(query?.toString() ?: "") }

            withContext(Dispatchers.Main) {
                adapter.items = potentialRemittees
            }
        }
    }

}