import SwiftUI
import fints4k
import BankFinder


struct AddAccountDialog: View {
    
    @State private var enteredBank = ""
    @State private var customerId = ""
    @State private var password = ""
    
    @State private var bank: BankInfo? = nil
    
    
    private let bankFinder = InMemoryBankFinder()
    
    
    var body: some View {
        let textValueBinding = Binding<String>(get: {
            self.enteredBank
        }, set: {
            self.enteredBank = $0
            self.findBank()
        })
        
        return NavigationView {
            Form {
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
            .hideNavigationBar()
        }
    }
    
    
    func findBank() {
        self.bank = bankFinder.findBankByNameBankCodeOrCity(query: enteredBank).first
    }
    
    func isRequiredDataEntered() -> Bool {
        return bank != nil
            && customerId.isEmpty == false
            && password.isEmpty == false
    }
    
    func addAccount() {
        
    }
}


struct AddAccountDialog_Previews: PreviewProvider {
    static var previews: some View {
        AddAccountDialog()
    }
}