import SwiftUI
import BankingUiSwift


struct AllBanksListItem: View {
    
    let banks: [Customer]
    
    
    var body: some View {
        Section {
            ZStack {
                HStack {
                    Text("All accounts")
                        .font(.headline)

                    Spacer()
                }.frame(height: 35)
                
                NavigationLink(destination: LazyView(AccountTransactionsDialog(allBanks: self.banks))) {
                    EmptyView()
                }
            }
        }
    }
}


struct AllBanksListItem_Previews: PreviewProvider {
    static var previews: some View {
        AllBanksListItem(banks: previewBanks)
    }
}
