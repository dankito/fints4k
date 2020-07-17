import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @State var banks: [BUCCustomer] = []
    
    
    init() {
        self.banks = presenter.customers
    }
    
    
    var body: some View {
        presenter.addAccountsChangedListener { (customers) in // i think this will add a lot of listeners but i am not allowed to use this code in init()
            self.banks = customers
        }
        
        return NavigationView {
            VStack {
                if self.banks.isEmpty == false {
                    List(banks, id: \.id) { bank in
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
        AccountsTab()
    }
}
