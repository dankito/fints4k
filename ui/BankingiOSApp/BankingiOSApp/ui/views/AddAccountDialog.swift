import SwiftUI
import BankingUiSwift
import Combine


struct AddAccountDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @State private var bank: BankInfo? = nil
    
    @State private var customerId = ""
    @State private var password = ""

    
    @State private var errorMessage: Message? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        Form {
            Section(header: Text("Bank")) {
                NavigationLink(destination: SelectBankDialog($bank)) {
                    if bank != nil {
                        bank.map { bank in
                            BankInfoListItem(bank: bank)
                        }
                    }
                    else {
                        Text("Select your bank ...")
                            .detailForegroundColor()
                            .frame(height: 50)
                    }
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Online banking login name", text: $customerId, placeholder: "Enter Online banking login name",
                                       focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress)
                
                LabelledUIKitTextField(label: "Online banking login password", text: $password, placeholder: "Enter Online banking login password",
                                       isPasswordField: true, actionOnReturnKeyPress: handleReturnKeyPress)
            }
            
            Section {
                HStack {
                    Spacer()
                    Button(action: { self.addAccount() },
                           label: { Text("Add") })
                        .disabled(!self.isRequiredDataEntered())
                    Spacer()
                }
            }
            
        }
        .alert(item: $errorMessage) { message in
            Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
        }
        .showNavigationBarTitle("Add account")
    }
    

    func handleReturnKeyPress() -> Bool {
        if self.isRequiredDataEntered() {
            self.addAccount()
            
            return true
        }
        
        return false
    }

    func isRequiredDataEntered() -> Bool {
        return bank != nil
            && customerId.isNotBlank
            && password.isNotBlank
    }
    
    func addAccount() {
        if let bank = bank {
            presenter.addAccountAsync(bankInfo: bank, customerId: customerId, pin: password) { (response) in
                self.handleAddAccountResponse(response)
            }
        }
    }
    
    func handleAddAccountResponse(_ response: AddAccountResponse) {
        if (response.isSuccessful) {
            DispatchQueue.main.async { // dispatch async as may EnterTanDialog is still displayed so dismiss() won't dismiss this view
                self.presentation.wrappedValue.dismiss()
            }
        }
        else {
            self.errorMessage = Message(title: Text("Could not add account"), message: Text("Error message from your bank \(response.errorToShowToUser ?? "")"))
        }
    }
}


struct AddAccountDialog_Previews: PreviewProvider {
    static var previews: some View {
        AddAccountDialog()
    }
}
