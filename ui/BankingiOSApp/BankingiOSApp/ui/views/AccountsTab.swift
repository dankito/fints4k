import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData


    var body: some View {
        VStack {
            if data.banks.isNotEmpty {
                Form {
                    AllBanksListItem(banks: data.banks)
                    
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
    }
    
}


struct AccountsTab_Previews: PreviewProvider {
    
    static var previews: some View {
        let data = AppData()
        data.banks = previewBanks
        
        return AccountsTab(data: data)
    }
    
}
