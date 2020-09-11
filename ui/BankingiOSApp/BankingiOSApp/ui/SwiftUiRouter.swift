import SwiftUI
import BankingUiSwift


class SwiftUiRouter : IRouter {
    
    
    func showAddAccountDialog(presenter: BankingPresenter) {
        
    }
    
    func getTanFromUserFromNonUiThread(customer: ICustomer, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: @escaping (EnterTanResult) -> Void) {
        let enterTanState = EnterTanState(customer, tanChallenge, callback)

        SceneDelegate.navigateToView(EnterTanDialog(enterTanState))
    }

    func getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: @escaping (EnterTanGeneratorAtcResult) -> Void) {
        callback(EnterTanGeneratorAtcResult.Companion().userDidNotEnterAtc())
    }
    
    func showTransferMoneyDialog(presenter: BankingPresenter, preselectedValues: TransferMoneyData?) {
        
    }
    
    func showSendMessageLogDialog(presenter: BankingPresenter) {
        
    }
    
}
