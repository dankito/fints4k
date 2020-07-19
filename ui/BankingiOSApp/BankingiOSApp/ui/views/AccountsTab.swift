import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData
    
    
    var body: some View {
        NavigationView {
            VStack {
                if data.banks.isEmpty == false {
                    Form {
                        ForEach(0 ..< data.banks.count) { bankIndex in
                            Section {
                                BankListItem(bank: self.data.banks[bankIndex])
                            }
                        }
                    }
                }
                
                Spacer()
                
                NavigationLink(destination: AddAccountDialog()) {
                    Text("Add account")
                }
                
                Spacer()
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
