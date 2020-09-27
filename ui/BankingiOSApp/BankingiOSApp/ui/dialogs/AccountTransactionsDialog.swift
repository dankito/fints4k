import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    static private let HideTopFetchAllTransactionsViewButtonWidth: CGFloat = 34


    private let title: String

    private let showBankIcons: Bool


    @State private var showTransactionsList = true

    @State private var noTransactionsFetchedMessage: LocalizedStringKey = ""

    @State private var showFetchTransactionsButton = true

    @State private var showFetchAllTransactionsView: Bool = false
    
    @State private var showFetchAllTransactionsViewAtTop: Bool = true

    @State private var accountsForWhichNotAllTransactionsHaveBeenFetched: [IBankAccount] = []


    @State private var filteredTransactions: [IAccountTransaction] = []

    @State private var balanceOfAllTransactions: CommonBigDecimal = CommonBigDecimal(decimal: "0")

    @State private var balanceOfFilteredTransactions: CommonBigDecimal = CommonBigDecimal(decimal: "0")
    
    @State private var searchText = ""

    private var searchTextBinding: Binding<String> {
        Binding<String>(
            get: { self.searchText },
            set: {
                self.searchText = $0
                self.filterTransactions($0)
        })
    }


    @State private var errorMessage: Message? = nil
    
    @State private var isInitialized = false


    @Inject private var presenter: BankingPresenterSwift


    init(allBanks: [IBankData]) {
        self.init("All accounts", true)

        presenter.selectedAllAccounts()
    }

    init(bank: IBankData) {
        self.init(bank.displayName, false)

        presenter.selectedBank(bank: bank)
    }

    init(account: IBankAccount) {
        self.init(account.displayName, false)

        presenter.selectedAccount(account: account)
    }

    fileprivate init(_ title: String, _ showBankIcons: Bool) {
        self.title = title

        self.showBankIcons = showBankIcons
    }


    var body: some View {
        VStack {
            Form {
                Section {
                    SearchBarWithLabel(searchTextBinding, returnKeyType: .done) {
                        HStack {
                            Text("\(String(self.filteredTransactions.count)) transactions")
                                .styleAsDetail()

                            Spacer()

                            AmountLabel(self.balanceOfFilteredTransactions)
                        }
                    }
                }

                if showFetchAllTransactionsView && showFetchAllTransactionsViewAtTop {
                    HStack(alignment: .center) {

                        fetchAllTransactionsButton
                            .padding(.horizontal, 0)

                    }
                    .padding(.horizontal, 0)
                    .padding(.trailing, Self.HideTopFetchAllTransactionsViewButtonWidth)
                    .frame(maxWidth: .infinity, minHeight: 44, maxHeight: .infinity) // has to have at least a height of 44 (iOS 14; iOS 13: 40), otherwise a white line at bottom gets displayed
                    .removeSectionBackground()
                    .overlay(hideTopFetchAllTransactionsViewButton, alignment: .trailing)
                }

                if showTransactionsList {
                    Section {
                        ForEach(filteredTransactions, id: \.technicalId) { transaction in
                            AccountTransactionListItem(transaction, self.showBankIcons)
                        }
                    }
                    
                    if showFetchAllTransactionsView && showFetchAllTransactionsViewAtTop == false {
                        SectionWithoutBackground {
                            HStack {
                                Spacer()
                                
                                fetchAllTransactionsButton
                            
                                Spacer()
                            }
                        }
                    }
                }
                else {
                    VStack(alignment: .center) {
                        Text(noTransactionsFetchedMessage)
                            .multilineTextAlignment(.center)

                        if showFetchTransactionsButton {
                            Button("Fetch transactions") { self.fetchTransactions() }
                                .padding(.top, 6)
                        }
                    }
                }
            }
            .systemGroupedBackground()
        }
        .executeMutatingMethod {
            if isInitialized == false {
                isInitialized = true
                
                self.setInitialValues()
            }
        }
        .alert(message: $errorMessage)
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarItems(trailing: UpdateButton { _, executingDone in self.updateTransactions(executingDone) })
    }


    private func setInitialValues() {
        self.balanceOfAllTransactions = self.presenter.balanceOfSelectedAccounts

        self.filterTransactions("")

        setTransactionsView()
    }

    private func setTransactionsView() {
        let transactionsRetrievalState = presenter.selectedAccountsTransactionRetrievalState
        let haveTransactionsForSelectedAccountsBeenRetrieved = transactionsRetrievalState == .retrievedtransactions

        self.accountsForWhichNotAllTransactionsHaveBeenFetched = presenter.selectedAccountsForWhichNotAllTransactionsHaveBeenFetched
        self.showFetchAllTransactionsView = accountsForWhichNotAllTransactionsHaveBeenFetched.isNotEmpty && haveTransactionsForSelectedAccountsBeenRetrieved
        //self.showFetchAllTransactionsViewAtTop = true // TODO: read from database


        self.showTransactionsList = haveTransactionsForSelectedAccountsBeenRetrieved

        self.noTransactionsFetchedMessage = getNoTransactionsFetchedMessage(transactionsRetrievalState)
        self.showFetchTransactionsButton = transactionsRetrievalState != .accountdoesnotsupportfetchingtransactions && transactionsRetrievalState != .accounttypenotsupported
    }
    
    private func hideTopFetchAllTransactionsView() {
        // TODO: save that we shouldn't show showFetchAllTransactionsView at top anymore in database
        for account in accountsForWhichNotAllTransactionsHaveBeenFetched {
            //UserDefaults.standard.set(true, forKey: Self.DoNotShowFetchAllTransactionsOverlayForUserDefaultsKeyPrefix + account.technicalId)
        }
        
        self.showFetchAllTransactionsViewAtTop = false
    }


    private var fetchAllTransactionsButton: some View {
        Button(action: { self.fetchAllTransactions() } ) {
            Text("Fetch all account transactions")
                .multilineTextAlignment(.center)
        }
        .foregroundColor(Color.blue)
    }
    
    private var hideTopFetchAllTransactionsViewButton: some View {
        // do not set Button's action as then if any of the two buttons get pressed, both actions get executed (bug in iOS). Use .onTapGesture() instead
        Button("X") { }
            .onTapGesture {
                self.hideTopFetchAllTransactionsView()
            }
            .frame(width: Self.HideTopFetchAllTransactionsViewButtonWidth, height: 44, alignment: .center)
    }


    private func updateTransactions(_ executingDone: @escaping () -> Void) {
        presenter.updateSelectedAccountsTransactionsAsync { response in
            executingDone()

            self.balanceOfAllTransactions = self.presenter.balanceOfSelectedAccounts

            if response.successful {
                self.filterTransactions(self.searchText)
            }
            else if response.userCancelledAction == false {
                if let failedAccount = getAccountThatFailed(response) {
                    self.errorMessage = Message(title: Text("Could not fetch latest transactions"), message: Text("Could not fetch latest transactions for \(failedAccount.displayName). Error message from your bank: \(response.errorToShowToUser ?? "")."))
                }
            }
        }
    }

    private func fetchTransactions() {
        for account in presenter.selectedAccounts {
            if account.haveAllTransactionsBeenRetrieved {
                presenter.updateAccountTransactionsAsync(account: account, abortIfTanIsRequired: false, callback: self.handleGetTransactionsResult)
            }
            else {
                presenter.fetchAllAccountTransactionsAsync(account: account, callback: self.handleGetTransactionsResult)
            }
        }
    }

    private func fetchAllTransactions() {
        accountsForWhichNotAllTransactionsHaveBeenFetched.forEach { account in
            presenter.fetchAllAccountTransactionsAsync(account: account, callback: self.handleGetTransactionsResult)
        }
    }

    private func handleGetTransactionsResult(_ response: GetTransactionsResponse) {
        setTransactionsView()

        if response.successful {
            self.filterTransactions(self.searchText)
        }
        else if response.userCancelledAction == false {
            if let failedAccount = getAccountThatFailed(response) {
                self.errorMessage = Message(title: Text("Could not fetch transactions"), message: Text("Could not fetch transactions for \(failedAccount.displayName). Error message from your bank: \(response.errorToShowToUser ?? "")."))
            }
        }
    }

    private func getNoTransactionsFetchedMessage(_ state: TransactionsRetrievalState) -> LocalizedStringKey {
        if state == .neverretrievedtransactions {
            return "No transactions fetched yet"
        }
        else if state == .notransactionsinretrievedperiod {
            let account = presenter.selectedAccounts.first!
            return "There haven't been any transactions in retrieved period from \(mapDate(account.retrievedTransactionsFromOn)) - \(mapDate(account.retrievedTransactionsUpTo))"
        }
        else if state == .accountdoesnotsupportfetchingtransactions {
            return "Account does not support retrieving transactions"
        }
        else {
            return "Account type not supported by app"
        }
    }

    private func mapDate(_ date: CommonDate?) -> String {
        if let date = date {
            return presenter.formatToMediumDate(date: date)
        }

        return ""
    }

    private func filterTransactions(_ query: String) {
        self.filteredTransactions = presenter.searchSelectedAccountTransactions(query: query).sorted { $0.valueDate.date > $1.valueDate.date }

        self.balanceOfFilteredTransactions = query.isBlank ? balanceOfAllTransactions : filteredTransactions.sumAmounts()
    }

    private func getAccountThatFailed(_ response: GetTransactionsResponse) -> IBankAccount? {
        return response.retrievedData.first { $0.successfullyRetrievedData == false }?.account
    }
    
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(previewBanks[0].displayName, false)
    }
}
