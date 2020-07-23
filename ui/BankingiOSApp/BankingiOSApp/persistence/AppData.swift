import Foundation
import BankingUiSwift


class AppData : ObservableObject {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Published var banks: [Customer] = []
    
    
    init() {
        banks = presenter.customers
        
        presenter.addAccountsChangedListener { banks in
            self.banks = banks
        }
    }
    
}
