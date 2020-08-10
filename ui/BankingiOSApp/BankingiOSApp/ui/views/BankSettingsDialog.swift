import SwiftUI
import BankingUiSwift
import UIKit


struct BankSettingsDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    private let bank: Customer
    
    @State private var displayName: String
    
    @State private var customerId: String
    @State private var password: String
    
    @State private var selectedTanProcedure: TanProcedure?
    
    @State private var unsavedChangesMessage: Message? = nil
    
    @State private var askToDeleteAccountMessage: Message? = nil
    
    
    private var hasUnsavedData: Bool {
        return bank.displayName != displayName
            || bank.customerId != customerId
            || bank.password != password
            || bank.selectedTanProcedure != selectedTanProcedure
    }
    
    
    init(_ bank: Customer) {
        self.bank = bank
        
        _displayName = State(initialValue: bank.displayName)
        
        _customerId = State(initialValue: bank.customerId)
        _password = State(initialValue: bank.password)
        
        _selectedTanProcedure = State(initialValue: bank.selectedTanProcedure)
    }

    
    var body: some View {
        Form {
            Section {
                LabelledUIKitTextField(label: "Name", text: $displayName)
            }
            
            Section(header: Text("Credentials")) {
                LabelledUIKitTextField(label: "Online banking login name", text: $customerId)
                
                LabelledUIKitTextField(label: "Online banking login password", text: $password, isPasswordField: true)
            }
            
            Section {
                TanProcedurePicker(bank) { selectedTanProcedure in
                    self.selectedTanProcedure = selectedTanProcedure
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Bank Code", value: bank.bankCode)
                
                LabelledUIKitTextField(label: "BIC", value: bank.bic)
                
                LabelledUIKitTextField(label: "Customer name", value: bank.customerName) // TODO: senseful?
                
                LabelledUIKitTextField(label: "FinTS server address", value: bank.finTsServerAddress) // TODO: senseful?
            }
            
            Section(header: Text("Accounts")) {
                ForEach(bank.accounts.sorted(by: { $0.displayIndex <= $1.displayIndex })) { account in
                    NavigationLink(destination: LazyView(BankAccountSettingsDialog(account))) {
                        Text(account.displayName)
                    }
                }
            }
            
            HStack {
                Spacer()
                
                Button("Delete account", action: askUserToDeleteAccount)
                    .foregroundColor(Color.red)
                
                Spacer()
            }
        }
        .alert(item: $unsavedChangesMessage) { message in
            Alert(title: message.title, message: message.message, primaryButton: message.primaryButton, secondaryButton: message.secondaryButton!)
        }
        .alert(item: $askToDeleteAccountMessage) { message in
            Alert(title: message.title, message: message.message, primaryButton: message.primaryButton, secondaryButton: message.secondaryButton!)
        }
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle(LocalizedStringKey(bank.displayName))
        .setCancelAndDoneNavigationBarButtons(onCancelPressed: cancelPressed, onDonePressed: donePressed)
    }
    
    
    func askUserToDeleteAccount() {
        self.askToDeleteAccountMessage = Message(title: Text("Delete account?"), message: Text("Really delete account '\(bank.displayName)'? This cannot be undone and data will be lost."), primaryButton: .destructive(Text("Delete"), action: deleteAccount), secondaryButton: .cancel())
    }
    
    func deleteAccount() {
        presenter.deleteAccount(customer: bank)
        
        closeDialog()
    }
    
    
    private func cancelPressed() {
        if hasUnsavedData {
            self.unsavedChangesMessage = Message(title: Text("Unsaved changes"), message: Text("Changed data hasn't been saved. Are you sure you want to discard them?"), primaryButton: .ok(closeDialog), secondaryButton: .cancel())
        }
        else {
            closeDialog()
        }
    }
    
    private func donePressed() {
        if hasUnsavedData {
            bank.userSetDisplayName = displayName
            
            bank.customerId = customerId
            bank.password = password
            
            bank.selectedTanProcedure = selectedTanProcedure
            
            presenter.accountUpdated(account: bank)
        }
        
        closeDialog()
    }
    
    private func closeDialog() {
        presentation.wrappedValue.dismiss()
    }

}


struct BankSettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        BankSettingsDialog(previewBanks[0])
    }

}
