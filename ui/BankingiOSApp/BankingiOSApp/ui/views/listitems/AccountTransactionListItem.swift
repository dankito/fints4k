import SwiftUI
import BankingUiSwift


struct AccountTransactionListItem: View {
    
    private let transaction: IAccountTransaction
    
    private let areMoreThanOneBanksTransactionsDisplayed: Bool
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ transaction: IAccountTransaction, _ areMoreThanOneBanksTransactionsDisplayed: Bool) {
        self.transaction = transaction
        
        self.areMoreThanOneBanksTransactionsDisplayed = areMoreThanOneBanksTransactionsDisplayed
    }
    
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(getTransactionLabel(transaction))
                    .font(.headline)
                    .lineLimit(1)
                    .frame(height: 20)
                
                Text(transaction.reference)
                    .styleAsDetail()
                    .padding(.top, 4)
                    .lineLimit(2)
                    .frame(height: 46, alignment: .center)
            }

            Spacer()

            VStack(alignment: .trailing) {
                if areMoreThanOneBanksTransactionsDisplayed {
                    IconView(iconUrl: transaction.account.bank.iconUrl, defaultIconName: Styles.AccountFallbackIcon)
                    
                    Spacer()
                }
                
                AmountLabel(transaction.amount, transaction.currency)
                
                Spacer()
                
                Text(presenter.formatToShortDate(date: transaction.valueDate))
                    .styleAsDetail()
            }
        }
        .contextMenu {
            if transaction.canCreateMoneyTransferFrom {
                Button(action: { self.navigateToTransferMoneyDialog(TransferMoneyData.Companion().fromAccountTransactionWithoutAmountAndReference(transaction: self.transaction)) }) {
                    HStack {
                        Text("Transfer money to \(transaction.otherPartyName ?? "")")
                        
                        Image("BankTransfer")
                    }
                }
                
                Button(action: { self.navigateToTransferMoneyDialog(TransferMoneyData.Companion().fromAccountTransaction(transaction: self.transaction)) }) {
                    HStack {
                        Text("New transfer with same data")
                        
                        Image("BankTransfer")
                    }
                }
            }
        }
        .onTapGesture {
            SceneDelegate.navigateToView(AccountTransactionDetailsDialog(transaction))
        }
    }


    private func getTransactionLabel(_ transaction: IAccountTransaction) -> String {
        if transaction.bookingText?.localizedCaseInsensitiveCompare("Bargeldauszahlung") == ComparisonResult.orderedSame {
            return transaction.bookingText ?? ""
        }

        if transaction.showOtherPartyName {
            return transaction.otherPartyName ?? ""
        }

        return transaction.bookingText ?? ""
    }
    
    private func navigateToTransferMoneyDialog(_ preselectedValues: TransferMoneyData) {
        SceneDelegate.navigateToView(TransferMoneyDialog(preselectedValues: preselectedValues))
    }

}

struct AccountTransactionListItem_Previews: PreviewProvider {
    static var previews: some View {
        AccountTransactionListItem(AccountTransaction(account: previewBanks[0].accounts[0] as! BankAccount, otherPartyName: "Marieke Musterfrau", unparsedReference: "Vielen Dank für Ihre Mühen", amount: CommonBigDecimal(double: 1234.56), valueDate: CommonDate(year: 2020, month: .march, day_: 27), bookingText: "SEPA Überweisung"), false)
    }
}
