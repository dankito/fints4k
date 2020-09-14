import SwiftUI
import BankingUiSwift


struct BankAccountSettingsDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    private let account: IBankAccount
    
    @State private var displayName: String
    
    @State private var unsavedChangesMessage: Message? = nil
    
    
    private var hasUnsavedData: Bool {
        return account.displayName != displayName
    }
    
    
    init(_ account: IBankAccount) {
        self.account = account
        
        _displayName = State(initialValue: account.displayName)
    }
    

    var body: some View {
        Form {
            Section {
                LabelledUIKitTextField(label: "Name", text: $displayName, autocapitalizationType: .none)
            }
            
            Section {
                LabelledValue("Account holder name", account.accountHolderName) // TODO: senseful?
                
                LabelledValue("Bank account identifier", account.identifier)
                
                account.subAccountNumber.map { subAccountNumber in
                    LabelledValue("Sub account number", subAccountNumber)
                }
                
                account.iban.map { iban in
                    LabelledValue("IBAN", iban)
                }
                
                LabelledValue("Bank account type", account.type.name) // TODO: senseful?
            }
            
            Section(header: Text("Supports")) {
                CheckmarkListItem("Supports Retrieving Balance", account.supportsRetrievingBalance)
                
                CheckmarkListItem("Supports Retrieving Account Transactions", account.supportsRetrievingAccountTransactions)
                
                CheckmarkListItem("Supports Transferring Money", account.supportsTransferringMoney)
                
                CheckmarkListItem("Supports Instant payment transfer", account.supportsInstantPaymentMoneyTransfer)
            }
        }
        .alert(message: $unsavedChangesMessage)
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle(LocalizedStringKey(account.displayName))
        .setCancelAndDoneNavigationBarButtons(onCancelPressed: cancelPressed, onDonePressed: donePressed)
    }
    
    
    private func cancelPressed() {
        if hasUnsavedData {
            self.unsavedChangesMessage = Message.createUnsavedChangesMessage(self.closeDialog)
        }
        else {
            closeDialog()
        }
    }
    
    private func donePressed() {
        if hasUnsavedData {
            account.userSetDisplayName = displayName
            
            presenter.accountUpdated(account: account)
        }
        
        closeDialog()
    }
    
    private func closeDialog() {
        presentation.wrappedValue.dismiss()
    }

}


struct BankAccountSettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        BankAccountSettingsDialog(previewBanks[0].accounts[0])
    }

}
