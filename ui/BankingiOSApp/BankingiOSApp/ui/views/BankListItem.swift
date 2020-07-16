import SwiftUI
import BankingUiSwift


struct BankListItem : View {
    
    var bank: BUCCustomer
    
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Text(bank.displayName)
                Spacer()
            }.frame(height: 35)
            
            List(bank.accounts, id: \.id) { account in
                return BankAccountListItem(account: account)
            }
        }.frame(minHeight: 70)
    }
    
}


struct BankListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankListItem(bank: previewBanks[0])
    }
}