import BankingUiSwift


class SwiftUiRouter : BUCIRouter {
    
    func showAddAccountDialog(presenter: BUCBankingPresenter) {
        
    }
    
    func getTanFromUserFromNonUiThread(customer: BUCCustomer, tanChallenge: BUCTanChallenge, presenter: BUCBankingPresenter) -> BUCEnterTanResult {
        return BUCEnterTanResult(enteredTan: nil,  changeTanProcedureTo: nil, changeTanMediumTo: nil, changeTanMediumResultCallback: nil)
    }
    
    func getAtcFromUserFromNonUiThread(tanMedium: BUCTanGeneratorTanMedium) -> BUCEnterTanGeneratorAtcResult {
        return BUCEnterTanGeneratorAtcResult(tan: nil, atc: nil)
    }
    
    func showTransferMoneyDialog(presenter: BUCBankingPresenter, preselectedBankAccount: BUCBankAccount?, preselectedValues: BUCTransferMoneyData?) {
        
    }
    
    func showSendMessageLogDialog(presenter: BUCBankingPresenter) {
        
    }

    
}
