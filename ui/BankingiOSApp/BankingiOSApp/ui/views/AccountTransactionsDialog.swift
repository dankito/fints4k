import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    private let title: String
    
    private let allTransactions: [AccountTransaction]
    
    private let balanceOfAllTransactions: CommonBigDecimal
    
    
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
    
    
    init(title: String, transactions: [AccountTransaction], balance: CommonBigDecimal) {
        self.title = title

        self.allTransactions = transactions
        self._filteredTransactions = State(initialValue: transactions)
        
        self.balanceOfAllTransactions = balance
        self._balanceOfFilteredTransactions = State(initialValue: balance)
    }
    
    
    var body: some View {
        VStack {
            UIKitSearchBar(text: searchTextBinding)
            
            HStack {
                Text("\(filteredTransactions.count) transactions")
                    .styleAsDetail()
                
                Spacer()
                
                Text(presenter.formatAmount(amount: balanceOfFilteredTransactions))
                    .styleAmount(amount: balanceOfFilteredTransactions)
            }
            .padding(.horizontal)
            
            List(filteredTransactions.sorted(by: { $0.valueDate.date > $1.valueDate.date } ), id: \.technicalId) { transaction in
                AccountTransactionListItem(transaction)
            }
        }
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarHidden(false)
    }
    
    
    private func filterTransactions(_ query: String) {
        self.filteredTransactions = presenter.searchAccountTransactions(query: query, transactions: allTransactions)
        
        self.balanceOfFilteredTransactions = query.isEmpty ? balanceOfAllTransactions : filteredTransactions.sumAmounts()
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
