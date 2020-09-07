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

                    ForEach(data.banks.sortedByDisplayIndex()) { bank in
                        BankListItem(bank: bank)
                    }

                    Section {
                        AddAccountButtonView()
                    }
                    .systemGroupedBackground()
                    .listRowInsets(EdgeInsets())

                }
            }
        }
        .systemGroupedBackground()
        .showNavigationBarTitle("Accounts")
        .navigationBarItems(leading: data.hasAtLeastOneAccountBeenAdded == false ? nil : UpdateButton { _, executingDone in
            self.presenter.updateAccountsTransactionsAsync { _ in executingDone() }
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
