import SwiftUI
import BankingUiSwift


struct AccountsTab: View {
    
    @Inject private var presenter: BankingPresenterSwift
    
    @ObservedObject var data: AppData


    var body: some View {
        VStack {
            if data.banks.isNotEmpty {
                Form {
                    AllBanksListItem(banks: data.banks)
                    
                    ForEach(data.banks) { bank in
                        BankListItem(bank: bank)
                    }
                }
            }
            
            Spacer()

            NavigationLink(destination: LazyView(AddAccountDialog())) {
                Text("Add account")
            }
            .frame(height: 35)
            
            Spacer()
        }
        .frame(width: UIScreen.main.bounds.width)
        .background(Color(UIColor.systemGroupedBackground))
    }

}


struct AccountsTab_Previews: PreviewProvider {
    
    static var previews: some View {
        let data = AppData()
        data.banks = previewBanks
        
        return AccountsTab(data: data)
    }
    
}
