import SwiftUI
import BankingUiSwift


struct BankAccountListItem : View {
    
    var account: BankAccount
    
    
    var body: some View {
        ZStack {
            HStack {
                Text(account.displayName)
                Spacer()
            }.frame(height: 35)
            
            NavigationLink(destination: AccountTransactionsDialog(account: account)) {
                EmptyView()
            }
        }
    }
    
}


struct BankAccountListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankAccountListItem(account: previewBanks[0].accounts[0])
    }
}
