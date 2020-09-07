import SwiftUI
import BankingUiSwift


struct BankListItem : View {

    let bank: Customer
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        Section {
            NavigationLink(destination: LazyView(AccountTransactionsDialog(bank: self.bank)), isActive: $navigateToAccountTransactionsDialog) {
                HStack {
                    IconedTitleView(bank, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(amount: bank.balance)
                }
                .frame(height: 35)
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


            ForEach(bank.accountsSorted) { account in
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

    private func deleteAccount(_ bank: Customer) {
        presenter.deleteAccount(customer: bank)
    }

}


struct BankListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankListItem(bank: previewBanks[0])
    }
}
