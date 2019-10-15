package net.dankito.banking.fints4java.android.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val transactionAdapter = AccountTransactionAdapter()


    private lateinit var presenter: MainWindowPresenter


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
                }
                else {
                    // TODO: show error
                }
            }
        }
    }

}