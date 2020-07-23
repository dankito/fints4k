import BankingUiSwift


class SwiftUiRouter : IRouter {
    
    
    func showAddAccountDialog(presenter: BankingPresenter) {
        
    }
    
    func getTanFromUserFromNonUiThread(customer: Customer, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: @escaping (EnterTanResult) -> Void) {
        callback(EnterTanResult.Companion().userDidNotEnterTan())
    }
    
    func getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: @escaping (EnterTanGeneratorAtcResult) -> Void) {
        callback(EnterTanGeneratorAtcResult.Companion().userDidNotEnterAtc())
    }
    
    func showTransferMoneyDialog(presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?) {
        
    }
    
    func showSendMessageLogDialog(presenter: BankingPresenter) {
        
    }
    
}
