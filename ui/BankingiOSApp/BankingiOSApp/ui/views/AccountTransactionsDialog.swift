import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    var title: String
    
    var transactions: [BUCAccountTransaction]
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        List(transactions, id: \.id) { transaction in
            HStack {
                VStack(alignment: .leading) {
                    Text(transaction.bookingText ?? "")
                    if transaction.showOtherPartyName {
                        Text(transaction.otherPartyName ?? "")
                    }
                    Text(transaction.usage)
                }
                
                Spacer()
                
                VStack(alignment: .trailing) {
                    Text(self.presenter.formatAmount(amount: transaction.amount))
                    //Text(transaction.valueDate)
                }
            }
        }
        .navigationBarTitle(Text(title), displayMode: NavigationBarItem.TitleDisplayMode.inline)
    }
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(title: previewBanks[0].displayName, transactions: [
            BUCAccountTransaction(bankAccount: previewBanks[0].accounts[0], amount: CommonBigDecimal(double: 1234.56), currency: "€", unparsedUsage: "Usage", bookingDate: CommonDate(year: 2020, month: 5, day: 7), otherPartyName: "Marieke Musterfrau", otherPartyBankCode: nil, otherPartyAccountId: nil, bookingText: "SEPA Ueberweisung", valueDate: CommonDate(year: 2020, month: 5, day: 7))
        ])
    }
}