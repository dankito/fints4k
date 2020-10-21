import SwiftUI
import BankingUiSwift


class SwiftUiRouter : IRouter {
    
    
    func showAddAccountDialog(presenter: BankingPresenter) {
        SceneDelegate.navigateToView(AddAccountDialog())
    }
    
    func getTanFromUserFromNonUiThread(bank: IBankData, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: @escaping (EnterTanResult) -> Void) {
        let enterTanState = EnterTanState(bank, tanChallenge, callback)

        SceneDelegate.navigateToView(EnterTanDialog(enterTanState))
    }

    func getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: @escaping (EnterTanGeneratorAtcResult) -> Void) {
        callback(EnterTanGeneratorAtcResult.Companion().userDidNotEnterAtc())
    }
    
    func showTransferMoneyDialog(presenter: BankingPresenter, preselectedValues: TransferMoneyData?) {
        SceneDelegate.navigateToView(TransferMoneyDialog(preselectedValues))
    }
    
    func showSendMessageLogDialog(presenter: BankingPresenter) {
        
    }
    
}
