import SwiftUI
import BankingUiSwift


struct AccountTransactionListItem: View {
    
    let transaction: AccountTransaction
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
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
}

struct AccountTransactionListItem_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionListItem(transaction: AccountTransaction(bankAccount: previewBanks[0].accounts[0], otherPartyName: "Marieke Musterfrau", unparsedUsage: "Vielen Dank für Ihre Mühen", amount: CommonBigDecimal(double: 1234.56), valueDate: CommonDate(year: 2020, month: .march, day_: 27), bookingText: "SEPA Überweisung"))
    }
}
