import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    private var title: String
    
    private var allTransactions: [AccountTransaction]
    
    
    @State private var filteredTransactions: [AccountTransaction]
    
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
    
    
    init(title: String, transactions: [AccountTransaction]) {
        self.title = title

        self.allTransactions = transactions
        self._filteredTransactions = State(initialValue: transactions)
    }
    
    
    var body: some View {
        Form {
            Section {
                UIKitSearchBar(text: searchTextBinding)
            }
            
            Section {
                List(filteredTransactions.sorted(by: { $0.valueDate.date > $1.valueDate.date } ), id: \.technicalId) { transaction in
                    AccountTransactionListItem(transaction)
                }
            }
        }
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarHidden(false)
    }
    
    
    private func filterTransactions(_ query: String) {
        self.filteredTransactions = presenter.searchAccountTransactions(query: query, transactions: allTransactions)
    }
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(title: previewBanks[0].displayName, transactions: [
            AccountTransaction(bankAccount: previewBanks[0].accounts[0], amount: CommonBigDecimal(double: 1234.56), currency: "€", unparsedUsage: "Usage", bookingDate: CommonDate(year: 2020, month: 5, day: 7), otherPartyName: "Marieke Musterfrau", otherPartyBankCode: nil, otherPartyAccountId: nil, bookingText: "SEPA Ueberweisung", valueDate: CommonDate(year: 2020, month: 5, day: 7))
        ])
    }
}
