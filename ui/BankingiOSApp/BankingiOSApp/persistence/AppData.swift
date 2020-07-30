import Foundation
import BankingUiSwift


class AppData : ObservableObject {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Published var banks: [Customer] = []
    
    @Published var hasAtLeastOneAccountBeenAdded: Bool = false
    
    
    init() {
        banks = presenter.customers
        hasAtLeastOneAccountBeenAdded = banks.isNotEmpty
        
        presenter.addAccountsChangedListener { banks in
            self.banks = banks
            self.hasAtLeastOneAccountBeenAdded = banks.isNotEmpty
        }
    }
    
}
