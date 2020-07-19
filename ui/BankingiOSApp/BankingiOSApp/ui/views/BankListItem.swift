import SwiftUI
import BankingUiSwift


struct BankListItem : View {
    
    var bank: BUCCustomer
    
    
    var body: some View {
        VStack {
            NavigationLink(destination: AccountTransactionsDialog(title: bank.displayName, transactions: bank.accounts.flatMap { $0.bookedTransactions })) {
                HStack {
                    Text(bank.displayName)
                        .font(.headline)
                    
                    Spacer()
                }.frame(height: 35)
            }
            
            VStack {
                ForEach(0 ..< bank.accounts.count) { accountIndex in
                    BankAccountListItem(account: self.bank.accounts[accountIndex])
                }
            }
            .padding(.leading, 18)
        }
    }
    
}


struct BankListItem_Previews: PreviewProvider {
    static var previews: some View {
        BankListItem(bank: previewBanks[0])
    }
}
