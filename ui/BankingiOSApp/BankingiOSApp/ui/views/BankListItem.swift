import SwiftUI
import BankingUiSwift


struct BankListItem : View {
    
    var bank: Customer
    
    
    var body: some View {
        Section {
            NavigationLink(destination: AccountTransactionsDialog(title: bank.displayName, transactions: bank.accounts.flatMap { $0.bookedTransactions })) {
                HStack {
                    Text(bank.displayName)
                        .font(.headline)

                    Spacer()
                }.frame(height: 35)
            }

            VStack {
                ForEach(bank.accounts) { account in
                    BankAccountListItem(account: account)
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
