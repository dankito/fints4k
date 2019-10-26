package net.dankito.banking.fints4java.android.ui.home

import android.app.SearchManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import net.dankito.banking.fints4java.android.MainActivity
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.adapter.AccountTransactionAdapter
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.utils.android.extensions.asActivity
import org.slf4j.LoggerFactory


class HomeFragment : Fragment() {

    companion object {
        private val log = LoggerFactory.getLogger(HomeFragment::class.java)
    }


    private lateinit var homeViewModel: HomeViewModel

    private lateinit var mnitmBalance: MenuItem

    private lateinit var mnitmSearchTransactions: MenuItem

    private val transactionAdapter = AccountTransactionAdapter()


    private lateinit var presenter: MainWindowPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(this, Observer {
//            textView.text = it
//        })


        val rcyvwAccountTransactions: RecyclerView = root.findViewById(R.id.rcyvwAccountTransactions)
        rcyvwAccountTransactions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rcyvwAccountTransactions.adapter = transactionAdapter

        initLogic()

        return root
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        menu?.let {
            mnitmBalance = menu.findItem(R.id.mnitmBalance)

            mnitmSearchTransactions = menu.findItem(R.id.mnitmSearchTransactions)

            initSearchView()

        }
    }

    private fun initSearchView() {
        context?.asActivity()?.let { context ->
            (context.getSystemService(Context.SEARCH_SERVICE) as? SearchManager)?.let { searchManager ->
                (mnitmSearchTransactions.actionView as? SearchView)?.let { searchView ->
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(context.componentName))

                    // if imeOptions aren't set like this searchView would take whole remaining screen when focused in landscape mode (see https://stackoverflow.com/questions/15296129/searchview-and-keyboard)
                    val searchInput =
                        searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as? EditText
                    searchInput?.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI

                    searchView.setOnQueryTextListener(searchAccountTransactionsTextListener)
                }
            }
        }
    }


    private fun initLogic() {

        // TODO: this is such a bad code style
        (context as? MainActivity)?.presenter?.let { presenter ->
            this.presenter = presenter

            presenter.addAccountAddedListener { bank, customer ->
                retrieveAccountTransactions(bank, customer)
            }
        }
    }

    private fun retrieveAccountTransactions(bank: BankData, customer: CustomerData) {
        presenter.getAccountTransactionsAsync(bank, customer) { response ->
            context?.asActivity()?.runOnUiThread {
                if (response.isSuccessful) {
                    transactionAdapter.items = response.bookedTransactions

                    response.balance?.let {
                        mnitmBalance.title = it.toString()
                    }
                }
                else {
                    // TODO: show error
                }
            }
        }
    }


    private val searchAccountTransactionsTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String): Boolean {
            searchAccountTransactions(query)
            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            return true
        }
    }

    private fun searchAccountTransactions(query: String) {
        transactionAdapter.items = presenter.searchAccountTransactions(query)
    }

}