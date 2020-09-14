import Foundation
import BankingUiSwift


class AppData : ObservableObject {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Published var banks: [ICustomer] = []
    @Published var banksSorted: [ICustomer] = []
    
    @Published var hasAtLeastOneAccountBeenAdded: Bool = false
    
    @Published var hasAccountsThatSupportTransferringMoney = false
    
    
    init() {
        setFieldsForBanks()
        
        presenter.addAccountsChangedListener { banks in
            self.setFieldsForBanks()
        }
    }
    
    
    private func setFieldsForBanks() {
        self.banks = presenter.customers
        self.banksSorted = banks.sortedByDisplayIndex()
        
        hasAtLeastOneAccountBeenAdded = banks.isNotEmpty
        hasAccountsThatSupportTransferringMoney = banks.flatMap { $0.accounts }.first(where: { $0.supportsTransferringMoney }) != nil
    }
    
    
    func banksDisplayIndexChanged() {
        self.banksSorted = banks.sortedByDisplayIndex()
    }
    
}
