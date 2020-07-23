import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData
    

    var body: some View {
        VStack {
            if data.banks.isEmpty == false {
                Form {
                    ForEach(data.banks) { bank in
                        BankListItem(bank: bank)
                    }
                }
            }

            Spacer()

            NavigationLink(destination: AddAccountDialog()) {
                Text("Add account")
            }

            Spacer()
        }
        .hideNavigationBar()
    }
    
}


struct AccountsTab_Previews: PreviewProvider {
    
    static var previews: some View {
        let data = AppData()
        data.banks = previewBanks
        
        return AccountsTab(data: data)
    }
    
}
