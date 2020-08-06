import Foundation
import BankingUiSwift


class AppData : ObservableObject {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Published var banks: [Customer] = []
    
    @Published var hasAtLeastOneAccountBeenAdded: Bool = false
    
    @Published var hasAccountsThatSupportTransferringMoney = false
    
    
    init() {
        setFieldsForBanks(presenter.customers)
        
        presenter.addAccountsChangedListener { banks in
            self.setFieldsForBanks(banks)
        }
    }
    
    
    private func setFieldsForBanks(_ banks: [Customer]) {
        self.banks = presenter.customers
        
        hasAtLeastOneAccountBeenAdded = banks.isNotEmpty
        hasAccountsThatSupportTransferringMoney = banks.flatMap { $0.accounts }.first(where: { $0.supportsTransferringMoney }) != nil
    }
    
}
