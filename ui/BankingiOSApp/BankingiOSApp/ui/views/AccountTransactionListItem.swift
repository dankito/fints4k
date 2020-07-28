import SwiftUI
import BankingUiSwift


struct AccountTransactionListItem: View {
    
    private static var ValueDateFormat: DateFormatter = {
        let formatter = DateFormatter()
        
        formatter.dateStyle = .short
        
        return formatter
    }()
    
    
    private let transaction: AccountTransaction
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ transaction: AccountTransaction) {
        self.transaction = transaction
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
                    .frame(height: 46, alignment: .center)
            }

            Spacer()

            VStack(alignment: .trailing) {
                AmountLabel(amount: transaction.amount)
                
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
