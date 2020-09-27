import SwiftUI
import BankingUiSwift


struct AccountTransactionDetailsDialog: View {
    
    private let transaction: IAccountTransaction
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ transaction: IAccountTransaction) {
        self.transaction = transaction
    }
    

    var body: some View {
        Form {
            if transaction.otherPartyName != nil || transaction.otherPartyAccountId != nil {
                Section(header: Text(transaction.amount.isPositive ? "Sender" : "Recipient")) {
                    LabelledValue("Name", transaction.otherPartyName)
                    
                    LabelledValue("IBAN", transaction.otherPartyAccountId) // TODO: check if it's really an IBAN
                    
                    LabelledValue("BIC", transaction.otherPartyBankCode) // TODO: check if it's really a BIC
                }
            }
            
            Section {
                LabelledAmount("Amount", transaction.amount, transaction.currency)
                
                VerticalLabelledValue("Reference", transaction.reference)
            }
            
            Section {
                LabelledValue("Booking text", transaction.bookingText ?? "")
                
                LabelledValue("Booking date", presenter.formatToMediumDate(date: transaction.bookingDate))
                
                LabelledValue("Value date", presenter.formatToMediumDate(date: transaction.valueDate))
                
                LabelledObject("Account") {
                    IconedTitleView(transaction.account)
                }
                
                if let openingBalance = transaction.openingBalance {
                    LabelledAmount("Account day opening balance", openingBalance, transaction.currency)
                }
                
                if let closingBalance = transaction.closingBalance {
                    LabelledAmount("Account day closing balance", closingBalance, transaction.currency)
                }
            }
        }
        .showNavigationBarTitle("Account Transaction Details Dialog Title")
    }

}


struct AccountTransactionDetailsDialog_Previews: PreviewProvider {

    static var previews: some View {
        AccountTransactionDetailsDialog(AccountTransaction(account: previewBanks[0].accounts[0] as! BankAccount, otherPartyName: "Other party name", unparsedReference: "Reference", amount: CommonBigDecimal(decimal: "84.23"), valueDate: CommonDate(year: 2020, month: 10, day: 21), bookingText: "Booking text"))
    }

}
