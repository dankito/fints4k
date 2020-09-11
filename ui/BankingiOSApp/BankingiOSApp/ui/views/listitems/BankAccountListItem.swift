import SwiftUI
import BankingUiSwift


struct BankAccountListItem : View {
    
    let account: IBankAccount
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    var body: some View {
        NavigationLink(destination: LazyView(AccountTransactionsDialog(account: self.account)), isActive: $navigateToAccountTransactionsDialog) {
            HStack {
                Text(account.displayName)
                
                Spacer()
                
                AmountLabel(amount: account.balance)
            }.frame(height: 35)
        }
        .contextMenu {
            Button(action: { self.navigateToBankAccountSettingsDialog() }) {
                HStack {
                    Text("Settings")
                    
                    Image(systemName: "gear")
                }
            }
        }
        .onTapGesture {
            self.navigateToAccountTransactionsDialog = true
        }
    }
    
    
    private func navigateToBankAccountSettingsDialog() {
        SceneDelegate.navigateToView(BankAccountSettingsDialog(account))
    }
    
}


struct BankAccountListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankAccountListItem(account: previewBanks[0].accounts[0])
    }
}
