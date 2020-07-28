import SwiftUI
import BankingUiSwift


struct BankAccountListItem : View {
    
    let account: BankAccount
    
    
    var body: some View {
        ZStack {
            HStack {
                Text(account.displayName)
                
                Spacer()
                
                AmountLabel(amount: account.balance)
            }.frame(height: 35)
            
            NavigationLink(destination: LazyView(AccountTransactionsDialog(account: self.account))) {
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
