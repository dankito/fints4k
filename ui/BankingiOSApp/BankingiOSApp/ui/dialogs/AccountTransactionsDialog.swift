import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    static private let DoNotShowFetchAllTransactionsOverlayForUserDefaultsKeyPrefix = "DoNotShowFetchAllTransactionsOverlayFor_"
    
    
    private let title: String
    
    private let allTransactions: [AccountTransaction]
    
    private let balanceOfAllTransactions: CommonBigDecimal
    
    private let areMoreThanOneBanksTransactionsDisplayed: Bool
    
    
    @State private var haveAllTransactionsBeenFetched: Bool
    
    @State private var showFetchAllTransactionsOverlay: Bool
    
    @State private var accountsForWhichNotAllTransactionsHaveBeenFetched: [BankAccount]
    
    
    @State private var filteredTransactions: [AccountTransaction]
    
    @State private var balanceOfFilteredTransactions: CommonBigDecimal
    
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


    init(allBanks: [Customer]) {
        let allAccounts = allBanks.flatMap { $0.accounts }
        
        self.init("All accounts", allAccounts.flatMap { $0.bookedTransactions }, allBanks.sumBalances(), allAccounts.filter { $0.haveAllTransactionsBeenFetched == false })

        presenter.selectedAllBankAccounts()
    }
    
    init(bank: Customer) {
        self.init(bank.displayName, bank.accounts.flatMap { $0.bookedTransactions }, bank.balance, bank.accounts.filter { $0.haveAllTransactionsBeenFetched == false })
        
        presenter.selectedAccount(customer: bank)
    }
    
    init(account: BankAccount) {
        self.init(account.displayName, account.bookedTransactions, account.balance, account.haveAllTransactionsBeenFetched ? [] : [account])
        
        presenter.selectedBankAccount(bankAccount: account)
    }
    
    fileprivate init(_ title: String, _ transactions: [AccountTransaction], _ balance: CommonBigDecimal, _ accountsForWhichNotAllTransactionsHaveBeenFetched: [BankAccount] = []) {
        self.title = title

        self.allTransactions = transactions
        self._filteredTransactions = State(initialValue: transactions)
        
        self.balanceOfAllTransactions = balance
        self._balanceOfFilteredTransactions = State(initialValue: balance)
        
        self.areMoreThanOneBanksTransactionsDisplayed = Set(allTransactions.compactMap { $0.bankAccount }.compactMap { $0.customer }).count > 1
        
        _accountsForWhichNotAllTransactionsHaveBeenFetched = State(initialValue: accountsForWhichNotAllTransactionsHaveBeenFetched)
        _haveAllTransactionsBeenFetched = State(initialValue: accountsForWhichNotAllTransactionsHaveBeenFetched.isEmpty)
        _showFetchAllTransactionsOverlay = State(initialValue: accountsForWhichNotAllTransactionsHaveBeenFetched.isNotEmpty)
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

                Section {
                    ForEach(filteredTransactions.sorted(by: { $0.valueDate.date > $1.valueDate.date } ), id: \.technicalId) { transaction in
                        AccountTransactionListItem(transaction, self.areMoreThanOneBanksTransactionsDisplayed)
                    }
                }
                     
                if haveAllTransactionsBeenFetched == false && showFetchAllTransactionsOverlay == false {
                    Section {
                        HStack {
                            Spacer()
                            
                            Button("Fetch all account transactions") {
                                 self.fetchAllTransactions(self.accountsForWhichNotAllTransactionsHaveBeenFetched)
                            }
                        
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
                        
                        Button(action: { self.fetchAllTransactions(self.accountsForWhichNotAllTransactionsHaveBeenFetched) }) {
                            Text("Fetch all account transactions")
                        }
                        
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
            self.showFetchAllTransactionsOverlay = self.shouldShowFetchAllTransactionsOverlay
        }
        .alert(message: $errorMessage)
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarItems(trailing: UpdateButton { _, executingDone in self.updateTransactions(executingDone) })
    }
    
    
    private func updateTransactions(_ executingDone: @escaping () -> Void) {
        presenter.updateSelectedBankAccountTransactionsAsync { response in
            executingDone()
            
            if response.isSuccessful {
                self.filterTransactions(self.searchText)
            }
            else if response.userCancelledAction == false {
                self.errorMessage = Message(title: Text("Could not fetch latest transactions"), message: Text("Could not fetch latest transactions for \(response.bankAccount.displayName). Error message from your bank: \(response.errorToShowToUser ?? "")."))
            }
        }
    }
    
    private func fetchAllTransactions(_ accounts: [BankAccount]) {
        accounts.forEach { account in
            presenter.fetchAllAccountTransactionsAsync(bankAccount: account, callback: self.handleGetAllTransactionsResult)
        }
    }
    
    private func handleGetAllTransactionsResult(_ response: GetTransactionsResponse) {
        self.accountsForWhichNotAllTransactionsHaveBeenFetched = self.accountsForWhichNotAllTransactionsHaveBeenFetched.filter { $0.haveAllTransactionsBeenFetched == false }
        self.haveAllTransactionsBeenFetched = self.accountsForWhichNotAllTransactionsHaveBeenFetched.isEmpty
        self.showFetchAllTransactionsOverlay = shouldShowFetchAllTransactionsOverlay
        
        if response.isSuccessful {
            self.filterTransactions(self.searchText)
        }
        else if response.userCancelledAction == false {
            self.errorMessage = Message(title: Text("Could not fetch all transactions"), message: Text("Could not fetch all transactions for \(response.bankAccount.displayName). Error message from your bank: \(response.errorToShowToUser ?? "")."))
        }
    }
    
    private func filterTransactions(_ query: String) {
        self.filteredTransactions = presenter.searchSelectedAccountTransactions(query: query)
        
        self.balanceOfFilteredTransactions = query.isBlank ? balanceOfAllTransactions : filteredTransactions.sumAmounts()
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
        AccountTransactionsDialog(previewBanks[0].displayName, [
            AccountTransaction(bankAccount: previewBanks[0].accounts[0], amount: CommonBigDecimal(double: 1234.56), currency: "€", unparsedUsage: "Usage", bookingDate: CommonDate(year: 2020, month: 5, day: 7), otherPartyName: "Marieke Musterfrau", otherPartyBankCode: nil, otherPartyAccountId: nil, bookingText: "SEPA Ueberweisung", valueDate: CommonDate(year: 2020, month: 5, day: 7))
            ],
            CommonBigDecimal(double: 84.12))
    }
}
