import SwiftUI
import BankingUiSwift


struct AllBanksListItem: View {
    
    let banks: [Customer]
    
    
    var body: some View {
        Section {
            NavigationLink(destination: LazyView(AccountTransactionsDialog(allBanks: self.banks))) {
                HStack {
                    IconedTitleView(accountTitle: "All accounts".localize(), iconUrl: nil, defaultIconName: Styles.AccountFallbackIcon, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(amount: banks.sumBalances())
                }.frame(height: 35)
            }
        }
    }
}


struct AllBanksListItem_Previews: PreviewProvider {
    static var previews: some View {
        AllBanksListItem(banks: previewBanks)
    }
}
