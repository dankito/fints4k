import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData
    
    
    var body: some View {
        NavigationView {
            VStack {
                if data.banks.isEmpty == false {
                    List(data.banks, id: \.id) { bank in
                        BankListItem(bank: bank)
                    }
                }
                
                NavigationLink(destination: AddAccountDialog()) {
                    Text("Add account")
                }
                .padding()
            }
            .navigationBarHidden(true)
            .navigationBarTitle(Text("Accounts"), displayMode: .inline)
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
