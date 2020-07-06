import SwiftUI


struct AddAccountDialog: View {
    
    @State private var bankCode = ""
    @State private var customerId = ""
    @State private var password = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Bank code", text: $bankCode)
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
    
    func isRequiredDataEntered() -> Bool {
        return bankCode.isEmpty == false
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