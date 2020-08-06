import SwiftUI
import BankingUiSwift


struct BankListItem : View {
    
    let bank: Customer
    
    
    var body: some View {
        Section {
            ZStack {
                HStack {
                    IconedTitleView(bank, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(amount: bank.balance)
                }.frame(height: 35)
                
                NavigationLink(destination: LazyView(AccountTransactionsDialog(bank: self.bank))) {
                    EmptyView()
                }
            }



            ForEach(bank.accounts) { account in
                BankAccountListItem(account: account)
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
