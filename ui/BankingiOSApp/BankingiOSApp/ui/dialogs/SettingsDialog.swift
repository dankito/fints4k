import SwiftUI
import BankingUiSwift


struct SettingsDialog: View {
    
    @Environment(\.editMode) var editMode

    @ObservedObject var data: AppData

    @Inject var presenter: BankingPresenterSwift

    
    @State private var updateAccountsAutomatically: Bool = true

    @State private var askToDeleteAccountMessage: Message? = nil
    
    
    init(_ data: AppData) {
        self.data = data
        
        self._updateAccountsAutomatically = State(initialValue: presenter.appSettings.updateAccountsAutomatically)
    }


    var body: some View {
        Form {
            Section(header: SectionHeaderWithRightAlignedEditButton("Bank Credentials", isEditButtonEnabled: data.hasAtLeastOneAccountBeenAdded),
                    footer: footer) {
                        ForEach(data.banksSorted, id: \.technicalId) { bank in
                    NavigationLink(destination: LazyView(BankSettingsDialog(bank))) {
                        IconedTitleView(bank)
                    }
                }
                .onMove(perform: reorderBanks)
                .onDelete(perform: deleteBanks)
            }
            
            Section {
                Toggle("Update accounts automatically", isOn: $updateAccountsAutomatically)
            }
            
            Section {
                NavigationLink(destination: EmptyView(), isActive: .constant(false)) { // we need custom navigation handling, so disable that NavigationLink takes care of navigating
                    Text("Secure app data")
                        .frame(maxWidth: .infinity, alignment: .leading) // stretch over full width
                        .background(Color.systemBackground) // make background tapable
                }
            }
            .onTapGesture {
                self.navigateToProtectAppSettingsDialog()
            }
        }
        .onDisappear { self.saveChanges() }
        .alert(message: $askToDeleteAccountMessage)
        .showNavigationBarTitle("Settings")
    }
    
    private var footer: some View {
        HStack {
            Spacer()
            
            NavigationLink(destination: LazyView(AddAccountDialog())) {
                Text("Add")
            }
        }
    }


    func reorderBanks(from sourceIndices: IndexSet, to destinationIndex: Int) {
        let _ = data.banksSorted.reorder(from: sourceIndices, to: destinationIndex)
        
        data.banksDisplayIndexChanged()
        
        presenter.allBanksUpdated()
    }

    func deleteBanks(at offsets: IndexSet) {
        for offset in offsets {
            let bankToDelete = data.banksSorted[offset]
            askUserToDeleteAccount(bankToDelete)
        }
    }

    func askUserToDeleteAccount(_ bankToDelete: IBankData) {
        self.askToDeleteAccountMessage = Message.createAskUserToDeleteAccountMessage(bankToDelete, self.deleteAccountWithSecurityChecks)
    }

    func deleteAccountWithSecurityChecks(_ bankToDelete: IBankData) {
        // don't know why but when deleting last bank application crashes if we don't delete bank async
        DispatchQueue.main.async {
            if self.presenter.allBanks.count == 1 {
                self.editMode?.wrappedValue = .inactive
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    self.deleteAccount(bankToDelete)
                }
            }
            else {
                self.deleteAccount(bankToDelete)
            }
        }
    }

    private func deleteAccount(_ bankToDelete: IBankData) {
        self.presenter.deleteAccount(bank: bankToDelete)
    }
    
    
    private func saveChanges() {
        if updateAccountsAutomatically != presenter.appSettings.updateAccountsAutomatically {
            presenter.appSettings.updateAccountsAutomatically = updateAccountsAutomatically
            presenter.appSettingsChanged()
        }
    }
    
    
    private func navigateToProtectAppSettingsDialog() {
        let authenticationService = AuthenticationService()
        
        if authenticationService.needsAuthenticationToUnlockApp == false {
            SceneDelegate.navigateToView(ProtectAppSettingsDialog())
        }
        else {
            let loginDialog = LoginDialog(authenticationService, allowCancellingLogin: true, loginReason: "Authenticate to change app protection settings") { authenticationSuccess in
                if authenticationSuccess {
                    SceneDelegate.navigateToView(ProtectAppSettingsDialog())
                }
            }
            
            SceneDelegate.navigateToView(loginDialog)
        }
    }

}


struct SettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        SettingsDialog(AppData())
    }

}
