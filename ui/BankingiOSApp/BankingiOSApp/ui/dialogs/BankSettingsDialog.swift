import SwiftUI
import BankingUiSwift
import UIKit


struct BankSettingsDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    private let bank: IBankData
    
    @State private var displayName: String
    
    @State private var customerId: String
    @State private var password: String
    
    @State private var selectedTanMethod: TanMethod?

    @State private var accountsSorted: [IBankAccount]
    
    @State private var askUserToDeleteAccountOrSaveChangesMessage: Message? = nil
    
    
    private var hasUnsavedData: Bool {
        return bank.displayName != displayName
            || bank.customerId != customerId
            || bank.password != password
            || bank.selectedTanMethod != selectedTanMethod
    }
    
    
    init(_ bank: IBankData) {
        self.bank = bank
        
        _displayName = State(initialValue: bank.displayName)
        
        _customerId = State(initialValue: bank.customerId)
        _password = State(initialValue: bank.password)
        
        _selectedTanMethod = State(initialValue: bank.selectedTanMethod)
        
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
                TanMethodPicker(bank) { selectedTanMethod in
                    self.selectedTanMethod = selectedTanMethod
                }
            }
            
            Section {
                LabelledValue("Bank Code", bank.bankCode)
                
                LabelledValue("BIC", bank.bic)
                
                LabelledValue("Customer name", bank.customerName) // TODO: senseful?
                
                LabelledValue("FinTS server address", bank.finTsServerAddress) // TODO: senseful?
            }
            
            Section(header: SectionHeaderWithRightAlignedEditButton("Accounts")) {
                ForEach(accountsSorted, id: \.technicalId) { account in
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
        
        presenter.bankDisplayIndexUpdated(bank: bank)
    }
    
    
    func askUserToDeleteAccount() {
        self.askUserToDeleteAccountOrSaveChangesMessage = Message.createAskUserToDeleteAccountMessage(bank, self.deleteAccount)
    }
    
    func deleteAccount(bank: IBankData) {
        presenter.deleteAccount(bank: bank)
        
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
            
            bank.selectedTanMethod = selectedTanMethod
            
            presenter.bankUpdated(bank: bank)
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
