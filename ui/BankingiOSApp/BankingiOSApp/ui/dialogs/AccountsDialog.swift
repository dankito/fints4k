import SwiftUI
import BankingUiSwift


struct AccountsDialog: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData


    var body: some View {
        VStack {
            if data.banks.isEmpty {
                Spacer()

                AddAccountButtonView()

                Spacer()
            }
            else {
                Form {
                    AllBanksListItem(banks: data.banks)

                    // if a constant id like \.technicalId is provided, list doesn't get updated on changes like balance changed or retrieved bank icon
                    ForEach(data.banksSorted, id: \.randomId) { bank in
                        BankListItem(bank: bank)
                    }

                    SectionWithoutBackground {
                        AddAccountButtonView()
                    }

                }
            }
        }
        .systemGroupedBackground()
        .showNavigationBarTitle("Accounts")
        .navigationBarItems(leading: data.hasAtLeastOneAccountBeenAdded == false ? nil : UpdateButton { _, executingDone in
            self.presenter.updateAllAccountsTransactionsAsync { _ in executingDone() }
        })
    }

}


struct AccountsDialog_Previews: PreviewProvider {
    
    static var previews: some View {
        let data = AppData()
        data.banks = previewBanks
        
        return AccountsDialog(data: data)
    }
    
}
