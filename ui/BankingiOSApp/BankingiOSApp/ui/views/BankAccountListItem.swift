import SwiftUI
import BankingUiSwift


struct BankAccountListItem : View {
    
    let account: BankAccount
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    var body: some View {
        NavigationLink(destination: LazyView(AccountTransactionsDialog(account: self.account)), isActive: $navigateToAccountTransactionsDialog) {
            HStack {
                Text(account.displayName)
                
                Spacer()
                
                AmountLabel(amount: account.balance)
            }.frame(height: 35)
        }
        .onTapGesture {
            self.navigateToAccountTransactionsDialog = true
        }
    }
    
}


struct BankAccountListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankAccountListItem(account: previewBanks[0].accounts[0])
    }
}
