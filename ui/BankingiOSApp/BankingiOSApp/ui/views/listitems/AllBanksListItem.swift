import SwiftUI
import BankingUiSwift


struct AllBanksListItem: View {
    
    let banks: [ICustomer]
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    var body: some View {
        Section {
            NavigationLink(destination: EmptyView(), isActive: .constant(false)) { // NavigationLink navigated to AccountTransactionsDialog twice. So i disabled NavigationLink and implemented manual navigation
                HStack {
                    IconedTitleView(accountTitle: "All accounts".localize(), iconUrl: nil, defaultIconName: Styles.AccountFallbackIcon, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(amount: banks.sumBalances())
                }
                .frame(height: 35)
                .background(Color.systemBackground) // make background tapable
            }
            .onTapGesture {
                SceneDelegate.navigateToView(AccountTransactionsDialog(allBanks: self.banks))
            }
        }
    }
}


struct AllBanksListItem_Previews: PreviewProvider {
    static var previews: some View {
        AllBanksListItem(banks: previewBanks)
    }
}
