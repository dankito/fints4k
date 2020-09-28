import SwiftUI
import BankingUiSwift


struct BankListItem : View {

    let bank: IBankData
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        Section {
            NavigationLink(destination: LazyView(AccountTransactionsDialog(bank: self.bank)), isActive: $navigateToAccountTransactionsDialog) {
                HStack {
                    IconedTitleView(bank, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(bank.balance)
                }
                .frame(height: 35)
                .background(Color.systemBackground) // make background tapable
                .contextMenu {
                    Button(action: { self.navigateToBankSettingsDialog() }) {
                        HStack {
                            Text("Settings")
                            
                            Image(systemName: "gear")
                        }
                    }
                    
                    Button(action: askUserToDeleteAccount) {
                        HStack {
                            Text("Delete account")

                            Image(systemName: "trash")
                        }
                    }
                }
                .onTapGesture {
                    self.navigateToAccountTransactionsDialog = true
                }
            }


            // if a constant id like \.technicalId is provided, list doesn't get updated on changes e.g. when balance changes
            ForEach(bank.visibleAccountsSorted, id: \.randomId) { account in
                BankAccountListItem(account: account)
            }
            .padding(.leading, Styles.AccountsIconWidth + Styles.DefaultSpaceBetweenIconAndText)
        }
    }
    
    
    private func navigateToBankSettingsDialog() {
        SceneDelegate.navigateToView(BankSettingsDialog(bank))
    }

    private func askUserToDeleteAccount() {
        // couldn't believe it, .alert() didn't work as SwiftUI resetted @State variable to dislpay it instantly, therefore Alert never got displayed
        // TODO: use values from Message.createAskUserToDeleteAccountMessage(self.bank, self.deleteAccount)
        UIAlert(
            "Really delete account '%@'?".localize(bank.displayName),
            "All data for this account will be permanently deleted locally.",
            UIAlertAction.destructive("Delete") { self.deleteAccount(self.bank) },
            UIAlertAction.cancel()
        ).show()
    }

    private func deleteAccount(_ bank: IBankData) {
        presenter.deleteAccount(bank: bank)
    }

}


struct BankListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankListItem(bank: previewBanks[0])
    }
}
