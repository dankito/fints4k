import SwiftUI
import BankingUiSwift


struct AccountTransactionListItem: View {
    
    private static var ValueDateFormat: DateFormatter = {
        let formatter = DateFormatter()
        
        formatter.dateStyle = .short
        
        return formatter
    }()
    
    
    private let transaction: AccountTransaction
    
    private let amountColor: Color
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ transaction: AccountTransaction) {
        self.transaction = transaction
        
        self.amountColor = transaction.amount.decimal.doubleValue < 0.0 ? Color.red : Color.green
    }
    
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(getTransactionLabel(transaction))
                    .font(.headline)
                    .frame(height: 20)
                
                Text(transaction.usage)
                    .styleAsDetail()
                    .padding(.top, 4)
                    .frame(height: 42, alignment: .center)
            }

            Spacer()

            VStack(alignment: .trailing) {
                Text(presenter.formatAmount(amount: transaction.amount))
                    .detailFont()
                    .foregroundColor(amountColor)
                
                Spacer()
                
                Text(Self.ValueDateFormat.string(from: transaction.valueDate.date))
                    .styleAsDetail()
            }
        }
    }


    private func getTransactionLabel(_ transaction: AccountTransaction) -> String {
        if transaction.bookingText?.localizedCaseInsensitiveCompare("Bargeldauszahlung") == ComparisonResult.orderedSame {
            return transaction.bookingText ?? ""
        }

        if transaction.showOtherPartyName {
            return transaction.otherPartyName ?? ""
        }

        return transaction.bookingText ?? ""
    }

}

struct AccountTransactionListItem_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionListItem(AccountTransaction(bankAccount: previewBanks[0].accounts[0], otherPartyName: "Marieke Musterfrau", unparsedUsage: "Vielen Dank für Ihre Mühen", amount: CommonBigDecimal(double: 1234.56), valueDate: CommonDate(year: 2020, month: .march, day_: 27), bookingText: "SEPA Überweisung"))
    }
}
