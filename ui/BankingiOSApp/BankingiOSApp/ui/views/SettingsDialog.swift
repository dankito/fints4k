import SwiftUI
import BankingUiSwift


struct SettingsDialog: View {
    
    @ObservedObject var data: AppData
    
    @Inject var presenter: BankingPresenterSwift
    

    var body: some View {
        Form {
            ForEach(data.banks.sorted(by: { $0.displayIndex >= $1.displayIndex })) { bank in
                NavigationLink(destination: LazyView(BankSettingsDialog(bank))) {
                    IconedTitleView(bank)
                }
            }
        }
    }

}


struct SettingsDialog_Previews: PreviewProvider {

    static var previews: some View {
        SettingsDialog(data: AppData())
    }

}
