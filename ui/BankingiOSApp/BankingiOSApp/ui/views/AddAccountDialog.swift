import SwiftUI
import BankingUiSwift


struct AddAccountDialog: View {
    
    @State private var enteredBank = ""
    @State private var customerId = ""
    @State private var password = ""
    
    @State private var bank: BankFinderBankInfo? = BankFinderBankInfo()
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
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
        self.bank = presenter.searchBanksByNameBankCodeOrCity(query: enteredBank).first
    }
    
    func isRequiredDataEntered() -> Bool {
        return bank != nil
            && customerId.isEmpty == false
            && password.isEmpty == false
    }
    
    func addAccount() {
        
        if let bank = bank {

            presenter.addAccountAsync(bankInfo: bank, customerId: customerId, pin: password) { (response: BUCAddAccountResponse) in
                NSLog("Is successful? \(response.isSuccessful), name = \(response.customer.customerName), \(response.customer.accounts.count) accounts, \(response.bookedTransactionsOfLast90Days.flatMap( { $1 }).count) transactions")
            }
        }
    }
}


struct AddAccountDialog_Previews: PreviewProvider {
    static var previews: some View {
        AddAccountDialog()
    }
}