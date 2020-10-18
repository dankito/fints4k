import SwiftUI
import BankingUiSwift


struct SettingsDialog: View {
    
    static private let Never = -1
    
    static private let AutomaticallyUpdateAccountsAfterOptions: [Int] = [ Never, 60, 2 * 60, 4 * 60, 6 * 60, 8 * 60, 10 * 60, 12 * 60, 24 * 60 ]
    
    static private let LockAppAfterOptions: [Int] = [ 0, 1, 2, 5, 10, 15, 30, 60, 2 * 60, 4 * 60, 8 * 60, 12 * 60, Never ]
    
    
    @Environment(\.editMode) var editMode

    @ObservedObject var data: AppData

    @Inject var presenter: BankingPresenterSwift
    
    @Inject private var authenticationService: AuthenticationService

    
    @State private var automaticallyUpdateAccountsAfterMinutesSelectedIndex: Int = 0
    
    @State private var lockAppAfterMinutesSelectedIndex: Int = 0

    @State private var askToDeleteAccountMessage: Message? = nil
    
    
    init(_ data: AppData) {
        self.data = data
        
        let settings = presenter.appSettings
        
        let automaticallyUpdateAccountsAfterMinutes = settings.automaticallyUpdateAccountsAfterMinutes != nil ? Int(settings.automaticallyUpdateAccountsAfterMinutes!) : Self.Never
        self._automaticallyUpdateAccountsAfterMinutesSelectedIndex = State(initialValue: Self.AutomaticallyUpdateAccountsAfterOptions.firstIndex(of: Int(automaticallyUpdateAccountsAfterMinutes)) ?? 0)
        
        let lockAppAfterMinutes = settings.lockAppAfterMinutes != nil ? Int(settings.lockAppAfterMinutes!) : Self.Never
        self._lockAppAfterMinutesSelectedIndex = State(initialValue: Self.LockAppAfterOptions.firstIndex(of: lockAppAfterMinutes) ?? 0)
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
                Picker("Automatically update accounts after", selection: $automaticallyUpdateAccountsAfterMinutesSelectedIndex) {
                    ForEach(0 ..< Self.AutomaticallyUpdateAccountsAfterOptions.count) { optionIndex in
                        Text(getDisplayTextForPeriod(Self.AutomaticallyUpdateAccountsAfterOptions[optionIndex]))
                    }
                }
                .disabled(true)
            }
            
            Section {
                NavigationLink(destination: EmptyView(), isActive: .constant(false)) { // we need custom navigation handling, so disable that NavigationLink takes care of navigating
                    Text("Secure app data")
                        .frame(maxWidth: .infinity, alignment: .leading) // stretch over full width
                        .makeBackgroundTapable()
                }
                .onTapGesture {
                    self.navigateToProtectAppSettingsDialog()
                }
                
                Picker("Lock app after", selection: $lockAppAfterMinutesSelectedIndex) {
                    ForEach(0 ..< Self.LockAppAfterOptions.count) { optionIndex in
                        Text(getDisplayTextForPeriod(Self.LockAppAfterOptions[optionIndex]))
                    }
                }
                .disabled(true)
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
    
    
    private func getDisplayTextForPeriod(_ periodInMinutes: Int) -> String {
        if periodInMinutes == 0 {
            return "Instantly".localize()
        }
        
        if periodInMinutes > 0 && periodInMinutes < 60 {
            return "%@ minutes".localize(String(periodInMinutes))
        }
        
        if periodInMinutes >= 60 {
            return "%@ hours".localize(String(periodInMinutes / 60))
        }
        
        return "Never".localize()
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
        let settings = presenter.appSettings
        
        let automaticallyUpdateAccountsAfterMinutes = getPeriod(Self.AutomaticallyUpdateAccountsAfterOptions, automaticallyUpdateAccountsAfterMinutesSelectedIndex)
        let lockAppAfterMinutes = getPeriod(Self.LockAppAfterOptions, lockAppAfterMinutesSelectedIndex)

        if automaticallyUpdateAccountsAfterMinutes != settings.automaticallyUpdateAccountsAfterMinutes
            || lockAppAfterMinutes != settings.lockAppAfterMinutes {
            settings.automaticallyUpdateAccountsAfterMinutes = automaticallyUpdateAccountsAfterMinutes
            settings.lockAppAfterMinutes = lockAppAfterMinutes
            presenter.appSettingsChanged()
        }
    }
    
    private func getPeriod(_ allValues: [Int], _ selectedIndex: Int) -> KotlinInt? {
        let value = allValues[selectedIndex]
        
        if value == Self.Never {
            return nil
        }
        
        return KotlinInt(int: Int32(value))
    }
    
    
    private func navigateToProtectAppSettingsDialog() {
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
