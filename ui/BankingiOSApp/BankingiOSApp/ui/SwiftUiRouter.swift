import SwiftUI
import BankingUiSwift


class SwiftUiRouter : IRouter {
    
    
    func showAddAccountDialog(presenter: BankingPresenter) {
        
    }
    
    func getTanFromUserFromNonUiThread(customer: Customer, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: @escaping (EnterTanResult) -> Void) {
        if let rootViewController = UIApplication.shared.windows.first(where: { (window) -> Bool in window.isKeyWindow})?.rootViewController as? UINavigationController {
            let enterTanState = EnterTanState(customer, tanChallenge, callback)

            let enterTanDialogController = UIHostingController(rootView: EnterTanDialog(enterTanState))

            rootViewController.pushViewController(enterTanDialogController, animated: true)
        }
    }

    func getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: @escaping (EnterTanGeneratorAtcResult) -> Void) {
        callback(EnterTanGeneratorAtcResult.Companion().userDidNotEnterAtc())
    }
    
    func showTransferMoneyDialog(presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?) {
        
    }
    
    func showSendMessageLogDialog(presenter: BankingPresenter) {
        
    }
    
}
