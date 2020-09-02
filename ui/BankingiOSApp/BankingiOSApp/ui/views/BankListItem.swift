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
            .padding(.leading, 18)
        }
    }
    

    func askUserToDeleteAccount() {
        // couldn't believe it, .alert() didn't work as SwiftUI resetted @State variable to dislpay it instantly, therefore Alert never got displayed
        // TODO: use values from Message.createAskUserToDeleteAccountMessage(self.bank, self.deleteAccount)
        let alert = UIAlertController(title: "Delete account?", message: "Really delete account '\(bank.displayName)'? This cannot be undone and data will be lost.", preferredStyle: .alert)

        alert.addAction(UIAlertAction(title: "Delete", style: .destructive, handler: { _ in self.deleteAccount(self.bank) } ))
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        
        if let rootViewController = SceneDelegate.rootNavigationController {
            rootViewController.present(alert, animated: true)
        }
    }

    func deleteAccount(_ bank: Customer) {
        presenter.deleteAccount(customer: bank)
    }

}


struct BankListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankListItem(bank: previewBanks[0])
    }
}
