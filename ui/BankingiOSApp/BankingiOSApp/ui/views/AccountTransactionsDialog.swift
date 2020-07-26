import SwiftUI
import BankingUiSwift


struct AccountTransactionsDialog: View {
    
    var title: String
    
    var transactions: [AccountTransaction]
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        List(transactions.sorted(by: { $0.valueDate.date > $1.valueDate.date } ), id: \.technicalId) { transaction in
            AccountTransactionListItem(transaction)
        }
        .showNavigationBarTitle(LocalizedStringKey(title))
        .navigationBarHidden(false)
    }
}


struct AccountTransactionsDialog_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionsDialog(title: previewBanks[0].displayName, transactions: [
            AccountTransaction(bankAccount: previewBanks[0].accounts[0], amount: CommonBigDecimal(double: 1234.56), currency: "€", unparsedUsage: "Usage", bookingDate: CommonDate(year: 2020, month: 5, day: 7), otherPartyName: "Marieke Musterfrau", otherPartyBankCode: nil, otherPartyAccountId: nil, bookingText: "SEPA Ueberweisung", valueDate: CommonDate(year: 2020, month: 5, day: 7))
        ])
    }
}
