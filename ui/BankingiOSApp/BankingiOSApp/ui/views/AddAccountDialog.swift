import SwiftUI
import BankingUiSwift


struct AddAccountDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @State private var enteredBank = ""
    @State private var customerId = ""
    @State private var password = ""
    
    @State private var bank: BankFinderBankInfo? = BankFinderBankInfo()
    
    @State private var errorMessage: Message? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        let textValueBinding = Binding<String>(get: {
            self.enteredBank
        }, set: {
            self.enteredBank = $0
            self.findBank()
        })
        
        return Form {
            Section {
                TextField("Bank code", text: textValueBinding)
            }
            
            Section {
                TextField("Customer ID", text: $customerId)
                
                SecureField("Password", text: $password)
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
            Alert(title: Text("Could not add account"), message: Text("Error message from your bank \(message.text)"), dismissButton: Alert.Button.cancel())
        }
        .navigationBarTitle(Text("Add account"), displayMode: NavigationBarItem.TitleDisplayMode.inline)
    }
    
    
    func findBank() {
        self.bank = presenter.searchBanksByNameBankCodeOrCity(query: enteredBank).first
    }
    
    func isRequiredDataEntered() -> Bool {
        return bank != nil
            && customerId.isEmpty == false
            && password.isEmpty == false
    }
    
    func addAccount() {
        if let bank = bank {
            presenter.addAccountAsync(bankInfo: bank, customerId: customerId, pin: password) { (response) in
                self.handleAddAccountResponse(response)
            }
        }
    }
    
    func handleAddAccountResponse(_ response: BUCAddAccountResponse) {
        if (response.isSuccessful) {
            presentation.wrappedValue.dismiss()
        }
        else {
            self.errorMessage = Message(text: (response.errorToShowToUser ?? ""))
        }
    }
}


struct AddAccountDialog_Previews: PreviewProvider {
    static var previews: some View {
        AddAccountDialog()
    }
}