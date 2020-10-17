package net.dankito.banking.ui.android.home

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.AccountTransactionAdapter
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.extensions.addHorizontalItemDivider
import net.dankito.banking.ui.android.extensions.showAmount
import net.dankito.banking.ui.android.views.InfoPopupWindow
import net.dankito.banking.ui.model.TransactionsRetrievalState
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.multiplatform.sum
import javax.inject.Inject


class HomeFragment : Fragment() {

    companion object {

        val TransactionsCannotBeRetrievedStates = listOf(TransactionsRetrievalState.AccountTypeNotSupported, TransactionsRetrievalState.AccountDoesNotSupportFetchingTransactions)

    }


    private lateinit var mnitmBalance: MenuItem

    private lateinit var mnitmSearchTransactions: MenuItem

    private lateinit var mnitmUpdateTransactions: MenuItem


    private var accountsForWhichNotAllTransactionsHaveBeenFetched = listOf<TypedBankAccount>()

    private var showTopFetchAllTransactionsView = true // TODO: read from db


    private val transactionAdapter: AccountTransactionAdapter

    protected var appliedTransactionsFilter = ""


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)

        transactionAdapter = AccountTransactionAdapter(presenter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val rcyvwAccountTransactions: RecyclerView = rootView.findViewById(R.id.rcyvwAccountTransactions)
        rcyvwAccountTransactions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rcyvwAccountTransactions.adapter = transactionAdapter
        rcyvwAccountTransactions.addHorizontalItemDivider()
        rcyvwAccountTransactions.isNestedScrollingEnabled = false

        registerForContextMenu(rcyvwAccountTransactions) // this is actually bad, splits code as context menu is created in AccountTransactionAdapter

        rootView.btnTopFetchAllTransactions.setOnClickListener {
            fetchAllTransactions()
        }

        rootView.btnBottomFetchAllTransactions.setOnClickListener {
            fetchAllTransactions()
        }

        rootView.btnShowFetchAllTransactionsInfo.setOnClickListener { showFetchAllTransactionsInfo(rootView.btnShowFetchAllTransactionsInfo) }

        rootView.btnHideTopFetchAllTransactionsView.setOnClickListener {
            hideTopFetchAllTransactionsView()
        }

        rootView.btnRetrieveTransactions.setOnClickListener { fetchTransactions() }
        rootView.btnAddAccount.setOnClickListener { presenter.showAddAccountDialog() }

        return rootView
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        mnitmBalance = menu.findItem(R.id.mnitmBalance)

        mnitmSearchTransactions = menu.findItem(R.id.mnitmSearchTransactions)
        mnitmUpdateTransactions = menu.findItem(R.id.mnitmUpdateTransactions)

        initSearchView()

        initLogicAfterUiInitialized()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnitmUpdateTransactions -> {
                updateAccountsTransactions()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initSearchView() {
        context?.asActivity()?.let { context ->
            (context.getSystemService(Context.SEARCH_SERVICE) as? SearchManager)?.let { searchManager ->
                (mnitmSearchTransactions.actionView as? SearchView)?.let { searchView ->
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(context.componentName))

                    // if imeOptions aren't set like this searchView would take whole remaining screen when focused in landscape mode (see https://stackoverflow.com/questions/15296129/searchview-and-keyboard)
                    val searchInput =
                        searchView.findViewById(androidx.appcompat.R.id.search_src_text) as? EditText
                    searchInput?.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI

                    searchView.setOnQueryTextListener(searchAccountTransactionsTextListener)
                }
            }
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnitmNewTransferToSameTransactionParty -> {
                newTransferToSameTransactionParty()
                return true
            }
            R.id.mnitmNewTransferWithSameData -> {
                newTransferWithSameData()
                return true
            }
        }

        return super.onContextItemSelected(item)
    }


    private fun initLogicAfterUiInitialized() {
        presenter.addBanksChangedListener { updateMenuItemsStateAndTransactionsToDisplay() } // on account addition or deletion may menu items' state changes
        presenter.addSelectedAccountsChangedListener { updateMenuItemsStateAndTransactionsToDisplay() }

        presenter.addRetrievedAccountTransactionsResponseListener { response ->
            handleGetTransactionsResponseOffUiThread(response)
        }

        updateMenuItemsStateAndTransactionsToDisplay()
    }


    private fun updateMenuItemsStateAndTransactionsToDisplay() {
        context?.asActivity()?.runOnUiThread {
            mnitmSearchTransactions.isVisible = presenter.doSelectedAccountsSupportRetrievingTransactions
            mnitmUpdateTransactions.isVisible = presenter.doSelectedAccountsSupportRetrievingTransactions

            updateTransactionsToDisplayOnUiThread()
        }
    }

    private fun updateAccountsTransactions() {
        presenter.updateSelectedAccountsTransactionsAsync()
    }

    private fun handleGetTransactionsResponseOffUiThread(response: GetTransactionsResponse) {
        context?.asActivity()?.let { activity ->
            activity.runOnUiThread {
                handleGetTransactionsResponseOnUiThread(activity, response)
            }
        }
    }

    private fun handleGetTransactionsResponseOnUiThread(context: Context, response: GetTransactionsResponse) {
        response.retrievedData.forEach { retrievedData ->
            if (retrievedData.successfullyRetrievedData) {
                updateTransactionsToDisplayOnUiThread()
            }
            else if (response.userCancelledAction == false) { // if user cancelled entering TAN then don't show a error message
                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.fragment_home_could_not_retrieve_account_transactions,
                        retrievedData.account.displayName, response.errorToShowToUser))
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }


    private fun newTransferToSameTransactionParty() {
        transactionAdapter.selectedTransaction?.let { selectedTransaction ->
            presenter.showTransferMoneyDialog(TransferMoneyData.fromAccountTransactionWithoutAmountAndReference(selectedTransaction))
        }
    }

    private fun newTransferWithSameData() {
        transactionAdapter.selectedTransaction?.let { selectedTransaction ->
            presenter.showTransferMoneyDialog(TransferMoneyData.fromAccountTransaction(selectedTransaction))
        }
    }


    private val searchAccountTransactionsTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String): Boolean {
            appliedTransactionsFilter = query

            updateTransactionsToDisplayOnUiThread()

            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            return true
        }
    }


    private fun updateTransactionsToDisplayOnUiThread() {
        transactionAdapter.items = presenter.searchSelectedAccountTransactions(appliedTransactionsFilter)

        mnitmBalance.title = presenter.formatAmount(presenter.balanceOfSelectedAccounts)
        mnitmBalance.isVisible = presenter.doSelectedAccountsSupportRetrievingBalance

        lytTransactionsSummary.visibility = if (presenter.doSelectedAccountsSupportRetrievingBalance) View.VISIBLE else View.GONE

        txtCountTransactions.text = context?.getString(R.string.fragment_home_count_transactions, transactionAdapter.items.size)

        val sumOfDisplayedTransactions = if (appliedTransactionsFilter.isBlank()) presenter.balanceOfSelectedAccounts
                                            else transactionAdapter.items.map { it.amount }.sum()
        txtTransactionsBalance.showAmount(presenter, sumOfDisplayedTransactions)

        setRecyclerViewAndNoTransactionsFetchedView()

        setFetchAllTransactionsView()
    }

    private fun setRecyclerViewAndNoTransactionsFetchedView() {
        val transactionsRetrievalState = presenter.selectedAccountsTransactionRetrievalState
        val haveTransactionsBeenRetrieved = transactionsRetrievalState == TransactionsRetrievalState.RetrievedTransactions
        val noAccountsAddedYet = presenter.allBanks.isEmpty()

        lytTransactionsTopBar.visibility = if (haveTransactionsBeenRetrieved) View.VISIBLE else View.GONE
        rcyvwAccountTransactions.visibility = if (haveTransactionsBeenRetrieved) View.VISIBLE else View.GONE
        lytNoTransactionsFetched.visibility = if (haveTransactionsBeenRetrieved || noAccountsAddedYet) View.GONE else View.VISIBLE
        btnRetrieveTransactions.visibility = if (TransactionsCannotBeRetrievedStates.contains(transactionsRetrievalState)) View.GONE else View.VISIBLE
        btnAddAccount.visibility = if (noAccountsAddedYet) View.VISIBLE else View.GONE

        val messageArgs = mutableListOf<String>()
        val transactionsRetrievalStateMessageId = when (transactionsRetrievalState) {
            TransactionsRetrievalState.AccountTypeNotSupported -> R.string.fragment_home_transactions_retrieval_state_account_type_not_supported
            TransactionsRetrievalState.AccountDoesNotSupportFetchingTransactions -> R.string.fragment_home_transactions_retrieval_state_account_does_not_support_retrieving_transactions
            TransactionsRetrievalState.NoTransactionsInRetrievedPeriod -> {
                val account = presenter.selectedAccounts.first()
                account.retrievedTransactionsFromOn?.let { messageArgs.add(presenter.formatToMediumDate(it)) }
                account.retrievedTransactionsUpTo?.let { messageArgs.add(presenter.formatToMediumDate(it)) }
                R.string.fragment_home_transactions_retrieval_state_no_transactions_in_retrieved_period
            }
            TransactionsRetrievalState.NeverRetrievedTransactions -> R.string.fragment_home_transactions_retrieval_state_never_retrieved_transactions
            else -> null
        }
        txtNoTransactionsFetchedMessage.text = transactionsRetrievalStateMessageId?.let { requireContext().getString(transactionsRetrievalStateMessageId, *messageArgs.toTypedArray()) } ?: ""
    }

    private fun setFetchAllTransactionsView() {
        accountsForWhichNotAllTransactionsHaveBeenFetched = presenter.selectedAccountsForWhichNotAllTransactionsHaveBeenFetched
        showTopFetchAllTransactionsView = presenter.showStrikingFetchAllTransactionsViewForSelectedAccounts
        val showFetchAllTransactionsView = presenter.showFetchAllTransactionsViewForSelectedAccounts

        if (showFetchAllTransactionsView && showTopFetchAllTransactionsView) {
            lytTopFetchAllTransactions.visibility = View.VISIBLE
        }
        else {
            lytTopFetchAllTransactions.visibility = View.GONE
        }

        if (showFetchAllTransactionsView && showTopFetchAllTransactionsView == false) {
            // TODO: implement CoordinatorLayout to show lytBottomFetchAllTransactions below rcyvwAccountTransactions
//            lytBottomFetchAllTransactions.visibility = View.VISIBLE
        }
        else {
            lytBottomFetchAllTransactions.visibility = View.GONE
        }
    }

    private fun hideTopFetchAllTransactionsView() {
        presenter.doNotShowStrikingFetchAllTransactionsViewAnymore(accountsForWhichNotAllTransactionsHaveBeenFetched)

        setFetchAllTransactionsView()
    }

    private fun showFetchAllTransactionsInfo(btnShowFetchAllTransactionsInfo: ImageButton) {
        activity?.let { activity ->
            val account = presenter.selectedAccountsForWhichNotAllTransactionsHaveBeenFetched.first()

            val dateOfFirstRetrievedTransaction = account.retrievedTransactionsFromOn?.let { presenter.formatToMediumDate(it) } ?: ""
            val info = activity.getString(R.string.popup_fetch_all_transactions_info, dateOfFirstRetrievedTransaction,
                account.countDaysForWhichTransactionsAreKept, presenter.formatToMediumDate(presenter.getDayOfFirstTransactionStoredOnBankServer(account)))

            InfoPopupWindow(activity, info).show(btnShowFetchAllTransactionsInfo, Gravity.BOTTOM)
        }
    }


    private fun fetchTransactions() {
        presenter.fetchTransactionsOfSelectedAccounts()
    }

    private fun fetchAllTransactions() {
        presenter.fetchAllTransactionsOfSelectedAccounts()
    }

}