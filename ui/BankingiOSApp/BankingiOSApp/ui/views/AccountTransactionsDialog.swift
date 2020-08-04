import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    private let title: String
    
    private let allTransactions: [AccountTransaction]
    
    private let balanceOfAllTransactions: CommonBigDecimal
    
    private let areMoreThanOneBanksTransactionsDisplayed: Bool
    
    
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
    
    
    @Inject private var presenter: BankingPresenterSwift


    init(allBanks: [Customer]) {
        self.init(title: "All accounts", transactions: allBanks.flatMap { $0.accounts }.flatMap { $0.bookedTransactions }, balance: allBanks.sumBalances())

        presenter.selectedAllBankAccounts()
    }
    
    init(bank: Customer) {
        self.init(title: bank.displayName, transactions: bank.accounts.flatMap { $0.bookedTransactions }, balance: bank.balance)
        
        presenter.selectedAccount(customer: bank)
    }
    
    init(account: BankAccount) {
        self.init(title: account.displayName, transactions: account.bookedTransactions, balance: account.balance)
        
        presenter.selectedBankAccount(bankAccount: account)
    }
    
    fileprivate init(title: String, transactions: [AccountTransaction], balance: CommonBigDecimal) {
        self.title = title

        self.allTransactions = transactions
        self._filteredTransactions = State(initialValue: transactions)
        
        self.balanceOfAllTransactions = balance
        self._balanceOfFilteredTransactions = State(initialValue: balance)
        
        self.areMoreThanOneBanksTransactionsDisplayed = Set(allTransactions.compactMap { $0.bankAccount }.compactMap { $0.customer }).count > 1
    }
    
    
    var body: some View {
        VStack {
            UIKitSearchBar(text: searchTextBinding)
            
            HStack {
                Text("\(filteredTransactions.count) transactions")
                    .styleAsDetail()
                
                Spacer()
                
                AmountLabel(amount: balanceOfFilteredTransactions)
            }
            .padding(.horizontal)
            
            List(filteredTransactions.sorted(by: { $0.valueDate.date > $1.valueDate.date } ), id: \.technicalId) { transaction in
                AccountTransactionListItem(transaction, self.areMoreThanOneBanksTransactionsDisplayed)
            }
        }
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarItems(trailing: UpdateButton { _ in self.retrieveTransactions() })
    }
    
    
    private func retrieveTransactions() {
        presenter.updateSelectedBankAccountTransactionsAsync { response in
            if response.isSuccessful {
                self.filterTransactions(self.searchText)
            }
            else if response.userCancelledAction == false {
                // TODO: show updating transactions failed message
            }
        }
    }
    
    private func filterTransactions(_ query: String) {
        self.filteredTransactions = presenter.searchSelectedAccountTransactions(query: query)
        
        self.balanceOfFilteredTransactions = query.isBlank ? balanceOfAllTransactions : filteredTransactions.sumAmounts()
    }
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(title: previewBanks[0].displayName, transactions: [
            AccountTransaction(bankAccount: previewBanks[0].accounts[0], amount: CommonBigDecimal(double: 1234.56), currency: "€", unparsedUsage: "Usage", bookingDate: CommonDate(year: 2020, month: 5, day: 7), otherPartyName: "Marieke Musterfrau", otherPartyBankCode: nil, otherPartyAccountId: nil, bookingText: "SEPA Ueberweisung", valueDate: CommonDate(year: 2020, month: 5, day: 7))
        ],
          balance: CommonBigDecimal(double: 84.12))
    }
}
