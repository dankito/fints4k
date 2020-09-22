import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    static private let DoNotShowFetchAllTransactionsOverlayForUserDefaultsKeyPrefix = "DoNotShowFetchAllTransactionsOverlayFor_"
    
    static private let RetrievedTransactionsPeriodDateFormat = DateFormatter()
    
    
    private let title: String
    
    private let showBankIcons: Bool
    
    
    @State private var haveTransactionsBeenRetrievedForSelectedAccounts = true
    
    @State private var haveAllTransactionsBeenFetched: Bool = false
    
    @State private var showTransactionsList = true
    
    @State private var noTransactionsFetchedMessage: LocalizedStringKey = ""
    
    @State private var showFetchTransactionsButton = true
    
    @State private var showFetchAllTransactionsOverlay: Bool = false
    
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
        
        Self.RetrievedTransactionsPeriodDateFormat.dateStyle = .medium
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
                            
                            AmountLabel(amount: self.balanceOfFilteredTransactions)
                        }
                    }
                }

                if showTransactionsList {
                    Section {
                        ForEach(filteredTransactions, id: \.technicalId) { transaction in
                            AccountTransactionListItem(transaction, self.showBankIcons)
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
                     
                if haveAllTransactionsBeenFetched == false && showFetchAllTransactionsOverlay == false && haveTransactionsBeenRetrievedForSelectedAccounts {
                    Section {
                        HStack {
                            Spacer()
                            
                            fetchAllTransactionsButton
                        
                            Spacer()
                        }
                    }
                    .frame(maxWidth: .infinity, minHeight: 40)
                    .systemGroupedBackground()
                    .listRowInsets(EdgeInsets())
                }
            }
            .systemGroupedBackground()

            if showFetchAllTransactionsOverlay {
                VStack {
                    Spacer()
                    
                    HStack(alignment: .center) {
                        Button(action: { self.doNotShowFetchAllTransactionsOverlayAnymore() }) {
                            Text("x")
                            .bold()
                        }
                        
                        Spacer()
                        
                        fetchAllTransactionsButton
                        
                        Spacer()
                    }
                    .padding(.horizontal, 6)
                    
                    Spacer()
                }
                .frame(height: 40)
                .padding(0)
                .systemGroupedBackground()
                .overlay(Divider(color: Color.gray), alignment: .top)
            }
        }
        .executeMutatingMethod {
            self.setInitialValues()
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
        self.haveTransactionsBeenRetrievedForSelectedAccounts = transactionsRetrievalState == .retrievedtransactions
        
        self.accountsForWhichNotAllTransactionsHaveBeenFetched = presenter.selectedAccountsForWhichNotAllTransactionsHaveBeenFetched
        self.haveAllTransactionsBeenFetched = self.accountsForWhichNotAllTransactionsHaveBeenFetched.isEmpty
        self.showFetchAllTransactionsOverlay = shouldShowFetchAllTransactionsOverlay && haveTransactionsBeenRetrievedForSelectedAccounts
        
        
        self.showTransactionsList = haveTransactionsBeenRetrievedForSelectedAccounts
        
        self.noTransactionsFetchedMessage = getNoTransactionsFetchedMessage(transactionsRetrievalState)
        self.showFetchTransactionsButton = transactionsRetrievalState != .accountdoesnotsupportfetchingtransactions && transactionsRetrievalState != .accounttypenotsupported
    }
    
    
    private var fetchAllTransactionsButton: some View {
        Button("Fetch all account transactions") {
             self.fetchAllTransactions(self.accountsForWhichNotAllTransactionsHaveBeenFetched)
        }
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
            if account.haveAllTransactionsBeenFetched {
                presenter.updateAccountTransactionsAsync(account: account, abortIfTanIsRequired: false, callback: self.handleGetTransactionsResult)
            }
            else {
                presenter.fetchAllAccountTransactionsAsync(account: account, callback: self.handleGetTransactionsResult)
            }
        }
    }
    
    private func fetchAllTransactions(_ accounts: [IBankAccount]) {
        accounts.forEach { account in
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
        if let date = date?.date {
            return Self.RetrievedTransactionsPeriodDateFormat.string(from: date)
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
    
    
    private func doNotShowFetchAllTransactionsOverlayAnymore() {
        for account in accountsForWhichNotAllTransactionsHaveBeenFetched {
            UserDefaults.standard.set(true, forKey: Self.DoNotShowFetchAllTransactionsOverlayForUserDefaultsKeyPrefix + account.technicalId)
        }
        
        showFetchAllTransactionsOverlay = false
    }
    
    private var shouldShowFetchAllTransactionsOverlay: Bool {
        if accountsForWhichNotAllTransactionsHaveBeenFetched.isNotEmpty {
            var copy = accountsForWhichNotAllTransactionsHaveBeenFetched
            
            copy.removeAll { UserDefaults.standard.bool(forKey: Self.DoNotShowFetchAllTransactionsOverlayForUserDefaultsKeyPrefix + $0.technicalId, defaultValue: false) }
            
            return copy.isNotEmpty
        }
        
        return false
    }
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(previewBanks[0].displayName, false)
    }
}
