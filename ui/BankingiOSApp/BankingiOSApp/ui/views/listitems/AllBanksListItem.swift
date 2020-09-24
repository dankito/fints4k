import SwiftUI
import BankingUiSwift


struct AllBanksListItem: View {
    
    let banks: [IBankData]
    
    @State private var navigateToAccountTransactionsDialog = false
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        Section {
            NavigationLink(destination: EmptyView(), isActive: .constant(false)) { // NavigationLink navigated to AccountTransactionsDialog twice. So i disabled NavigationLink and implemented manual navigation
                HStack {
                    IconedTitleView(accountTitle: "All accounts".localize(), iconUrl: nil, defaultIconName: Styles.AccountFallbackIcon, titleFont: .headline)

                    Spacer()
                    
                    AmountLabel(banks.sumBalances(), presenter.currencyIsoCodeOfAccounts(accounts: presenter.allAccounts))
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
