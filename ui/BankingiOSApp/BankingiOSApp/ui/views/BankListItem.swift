import SwiftUI
import BankingUiSwift


struct BankListItem : View {
    
    let bank: Customer
    
    
    var body: some View {
        Section {
            ZStack {
                HStack {
                    Text(bank.displayName)
                        .font(.headline)

                    Spacer()
                }.frame(height: 35)
                
                NavigationLink(destination: LazyView(AccountTransactionsDialog(bank: self.bank))) {
                    EmptyView()
                }
            }


            ForEach(0 ..< bank.accounts.count) { accountIndex in
                BankAccountListItem(account: self.bank.accounts[accountIndex])
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
