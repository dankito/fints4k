import Foundation
import BankingUiSwift


class AppData : ObservableObject {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Published var banks: [IBankData] = []
    @Published var banksSorted: [IBankData] = []
    
    @Published var hasAtLeastOneAccountBeenAdded: Bool = false
    
    @Published var hasAccountsThatSupportTransferringMoney = false
    
    
    init() {
        setFieldsForBanks()
        
        presenter.addBanksChangedListener { banks in
            self.setFieldsForBanks()
        }
    }
    
    
    private func setFieldsForBanks() {
        self.banks = presenter.allBanks
        self.banksSorted = banks.sortedByDisplayIndex()
        
        hasAtLeastOneAccountBeenAdded = banks.isNotEmpty
        hasAccountsThatSupportTransferringMoney = banks.flatMap { $0.accounts }.first(where: { $0.supportsTransferringMoney }) != nil
    }
    
    
    func banksDisplayIndexChanged() {
        self.banksSorted = banks.sortedByDisplayIndex()
    }
    
}
