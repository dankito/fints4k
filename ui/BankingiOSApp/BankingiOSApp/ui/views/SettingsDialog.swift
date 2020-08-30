import SwiftUI
import BankingUiSwift


struct SettingsDialog: View {

    @ObservedObject var data: AppData

    @Inject var presenter: BankingPresenterSwift


    @State private var askToDeleteAccountMessage: Message? = nil


    var body: some View {
        Form {
            Section(header: EditButton().frame(maxWidth: .infinity, alignment: .trailing)
            .overlay(Text("Bank Credentials"), alignment: .leading)) {
                ForEach(data.banksSorted) { bank in
                    NavigationLink(destination: LazyView(BankSettingsDialog(bank))) {
                        IconedTitleView(bank)
                    }
                }
                .onMove(perform: reorderBanks)
                .onDelete(perform: deleteBanks)
            }
        }
        .alert(item: $askToDeleteAccountMessage) { message in
            Alert(title: message.title, message: message.message, primaryButton: message.primaryButton, secondaryButton: message.secondaryButton!)
        }
        .showNavigationBarTitle("Settings")
    }


    func reorderBanks(from sourceIndices: IndexSet, to destinationIndex: Int) {
        let _ = data.banksSorted.reorder(from: sourceIndices, to: destinationIndex)
        
        data.banksDisplayIndexChanged()
        
        presenter.allAccountsUpdated()
    }

    func deleteBanks(at offsets: IndexSet) {
        for offset in offsets {
            let bankToDelete = data.banksSorted[offset]
            askUserToDeleteAccount(bankToDelete)
        }
    }

    func askUserToDeleteAccount(_ bankToDelete: Customer) {
        self.askToDeleteAccountMessage = Message(title: Text("Delete account?"), message: Text("Really delete account '\(bankToDelete.displayName)'? This cannot be undone and data will be lost."), primaryButton: .destructive(Text("Delete"), action: { self.deleteAccount(bankToDelete) }), secondaryButton: .cancel())
    }

    func deleteAccount(_ bankToDelete: Customer) {
        presenter.deleteAccount(customer: bankToDelete)
    }

}


struct SettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        SettingsDialog(data: AppData())
    }

}
