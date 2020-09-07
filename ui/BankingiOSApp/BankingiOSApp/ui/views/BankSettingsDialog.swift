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
    
    @State private var accountsSorted: [BankAccount]
    
    @State private var askUserToDeleteAccountOrSaveChangesMessage: Message? = nil
    
    
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
        
        _accountsSorted = State(initialValue: bank.accountsSorted)
    }

    
    var body: some View {
        Form {
            Section {
                LabelledUIKitTextField(label: "Name", text: $displayName)
            }
            
            Section(header: Text("Credentials")) {
                LabelledUIKitTextField(label: "Online banking login name", text: $customerId, autocapitalizationType: .none)
                
                LabelledUIKitTextField(label: "Online banking login password", text: $password, autocapitalizationType: .none, isPasswordField: true)
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
            
            Section(header: SectionHeaderWithRightAlignedEditButton("Accounts")) {
                ForEach(accountsSorted) { account in
                    NavigationLink(destination: LazyView(BankAccountSettingsDialog(account))) {
                        Text(account.displayName)
                    }
                }
                .onMove(perform: reorderAccounts)
            }
            
            HStack {
                Spacer()
                
                Button("Delete account", action: askUserToDeleteAccount)
                    .foregroundColor(Color.destructive)
                
                Spacer()
            }
        }
        .alert(message: $askUserToDeleteAccountOrSaveChangesMessage)
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle(LocalizedStringKey(bank.displayName))
        .setCancelAndDoneNavigationBarButtons(onCancelPressed: cancelPressed, onDonePressed: donePressed)
    }


    func reorderAccounts(from source: IndexSet, to destination: Int) {
        accountsSorted = accountsSorted.reorder(from: source, to: destination)
        
        presenter.accountUpdated(account: bank)
    }
    
    
    func askUserToDeleteAccount() {
        self.askUserToDeleteAccountOrSaveChangesMessage = Message.createAskUserToDeleteAccountMessage(bank, self.deleteAccount)
    }
    
    func deleteAccount(bank: Customer) {
        presenter.deleteAccount(customer: bank)
        
        closeDialog()
    }
    
    
    private func cancelPressed() {
        if hasUnsavedData {
            self.askUserToDeleteAccountOrSaveChangesMessage = Message.createUnsavedChangesMessage(self.closeDialog)
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
