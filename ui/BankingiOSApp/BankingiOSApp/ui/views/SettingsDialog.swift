import SwiftUI
import BankingUiSwift


struct SettingsDialog: View {

    @ObservedObject var data: AppData

    @Inject var presenter: BankingPresenterSwift


    @State private var askToDeleteAccountMessage: Message? = nil


    var body: some View {
        Form {
            Section(header: SectionHeaderWithRightAlignedEditButton("Bank Credentials", isEditButtonEnabled: data.hasAtLeastOneAccountBeenAdded),
                    footer: footer) {
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
    
    private var footer: some View {
        get {
            HStack {
                Spacer()
                
                NavigationLink(destination: LazyView(AddAccountDialog())) {
                    Text("Add")
                }
            }
        }
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
        self.askToDeleteAccountMessage = Message.createAskUserToDeleteAccountMessage(bankToDelete, self.deleteAccount)
    }

    func deleteAccount(_ bankToDelete: Customer) {
        // don't know why but when deleting last bank application crashes if we don't delete bank async
        DispatchQueue.main.async {
            self.presenter.deleteAccount(customer: bankToDelete)
        }
    }

}


struct SettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        SettingsDialog(data: AppData())
    }

}
