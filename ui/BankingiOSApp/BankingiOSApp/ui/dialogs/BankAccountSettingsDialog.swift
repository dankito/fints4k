import SwiftUI
import BankingUiSwift


struct BankAccountSettingsDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    private let account: IBankAccount
    
    @State private var displayName: String
    
    @State private var hideAccount: Bool
    
    @State private var updateAccountAutomatically: Bool
    
    @State private var unsavedChangesMessage: Message? = nil
    
    
    private var hasUnsavedData: Bool {
        return account.displayName != displayName
            || account.hideAccount != hideAccount
            || account.updateAccountAutomatically != updateAccountAutomatically
    }
    
    
    init(_ account: IBankAccount) {
        self.account = account
        
        _displayName = State(initialValue: account.displayName)
        _hideAccount = State(initialValue: account.hideAccount)
        _updateAccountAutomatically = State(initialValue: account.updateAccountAutomatically)
    }
    

    var body: some View {
        Form {
            Section {
                LabelledUIKitTextField(label: "Name", text: $displayName, autocapitalizationType: .none)
                
                Toggle("Hide bank account", isOn: $hideAccount)
                
                Toggle("Update bank account automatically", isOn: $updateAccountAutomatically)
                    .disabled(hideAccount)
            }
            
            Section(header: shareButton.alignHorizontally(.trailing)) {
                LabelledValue("Account holder name", account.accountHolderName) // TODO: senseful?
                
                LabelledValue("Bank account identifier", account.identifier)
                
                account.subAccountNumber.map { subAccountNumber in
                    LabelledValue("Sub account number", subAccountNumber)
                }
                
                account.iban.map { iban in
                    LabelledValue("IBAN", iban)
                }
                
                LabelledValue("Bank account type", getBankAccountKey(account.type).localize())
            }
            
            Section(header: Text("Supports")) {
                CheckmarkListItem("Supports Retrieving Balance", account.supportsRetrievingBalance)
                
                CheckmarkListItem("Supports Retrieving Account Transactions", account.supportsRetrievingAccountTransactions)
                
                CheckmarkListItem("Supports Transferring Money", account.supportsTransferringMoney)
                
                CheckmarkListItem("Supports Real-time transfer", account.supportsRealTimeTransfer)
            }
        }
        .alert(message: $unsavedChangesMessage)
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle(LocalizedStringKey(account.displayName))
        .setCancelAndDoneNavigationBarButtons(onCancelPressed: cancelPressed, onDonePressed: donePressed)
    }
    
    
    private var shareButton: some View {
        Button(action: self.shareAccountData) {
            Image(systemName: "square.and.arrow.up")
                .resizable()
                .scaledToFill()
                .frame(width: 20, height: 20)
                .linkForegroundColor()
        }
        .padding(.trailing, -6)
        .padding(.bottom, 4)
    }
    
    private func getBankAccountKey(_ type: BankAccountType) -> String {
        switch type {
        case .checkingaccount:
            return "Checking account"
        case .savingsaccount:
            return "Savings account"
        case .fixedtermdepositaccount:
            return "Fixed term deposit account"
        case .securitiesaccount:
            return "Securities account"
        case .loanaccount:
            return "Loan account"
        case .creditcardaccount:
            return "Credit card account"
        case .funddeposit:
            return "Fund deposit"
        case .buildingloancontract:
            return "Building loan contract"
        case .insurancecontract:
            return "Insurance contract"
        default:
            return "Other"
        }
    }
    
    
    private func shareAccountData() {
        var accountData = account.accountHolderName + "\n" + account.bank.bankName
        
        if let iban = account.iban {
            accountData.append("\n" + "IBAN \(iban)".localize())
        }
        
        accountData.append("\n" + "BIC \(account.bank.bic)".localize())
        accountData.append("\n" + "Bank code \(account.bank.bankCode)".localize())
        accountData.append("\n" + "Account number \(account.identifier)".localize())
        
        let attributes = [NSAttributedString.Key : Any]()
        let string = NSAttributedString(string: accountData, attributes: attributes)
        let print = UISimpleTextPrintFormatter(attributedText: string)
        
        
        let activityViewController = UIActivityViewController(activityItems: [ accountData, print ], applicationActivities: nil)
        
        let viewController = SceneDelegate.rootViewController
        
        // needed for iPad
        activityViewController.popoverPresentationController?.sourceView = viewController?.view
        
        viewController?.present(activityViewController, animated: true, completion: nil)
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
            
            account.hideAccount = hideAccount
            account.updateAccountAutomatically = updateAccountAutomatically
            
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
