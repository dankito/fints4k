import SwiftUI
import BankingUiSwift
import Combine


struct AddAccountDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @State private var enteredBank = ""
    @State private var customerId = ""
    @State private var password = ""
    
    @State private var bank: BankFinderBankInfo? = BankFinderBankInfo()
    
    @State private var errorMessage: Message? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        return Form {
            Section {
                TextField("Bank code", text: $enteredBank)
                    .onReceive(Just(enteredBank)) { newValue in
                        self.findBank()
                }
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
            Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
        }
        .navigationBarTitle(Text("Add account"), displayMode: NavigationBarItem.TitleDisplayMode.inline)
        .navigationBarHidden(false)
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
    
    func handleAddAccountResponse(_ response: AddAccountResponse) {
        if (response.isSuccessful) {
            presentation.wrappedValue.dismiss()
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
