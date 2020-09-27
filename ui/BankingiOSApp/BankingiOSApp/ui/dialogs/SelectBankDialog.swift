import SwiftUI
import BankingUiSwift


struct SelectBankDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    @Inject private var presenter: BankingPresenterSwift

    
    @Binding private var selectedBank: BankInfo?
    
    @State private var searchText = ""
    
    private var searchTextBinding: Binding<String> {
        Binding<String>(
            get: { self.searchText },
            set: {
                self.searchText = $0
                self.findBanks($0)
        })
    }
    
    @State private var supportedBanksSearchResults: [BankInfo] = []
    
    @State private var unsupportedBanksSearchResults: [BankInfo] = []
    
    @State private var isInitialized = false

    
    @State private var errorMessage: Message? = nil
    
    
    init(_ selectedBank: Binding<BankInfo?>) {
        self._selectedBank = selectedBank
    }
    
    
    var body: some View {
        Form {
            Section {
                SearchBarWithLabel(searchTextBinding, placeholder: "Bank code, bank name or city", focusOnStart: true, returnKeyType: .done) {
                    Text("Search by bank code, bank name or city")
                        .font(.caption)
                        .styleAsDetail()
                        .alignHorizontally(.leading)
                }
            }
            
            if supportedBanksSearchResults.isEmpty {
                Text("No supported banks found")
                    .detailForegroundColor()
                    .alignHorizontally(.center)
            }
            else {
                Section {
                    // TODO: showing only the first 100 items is a workaround as SwiftUI tries to compare the two lists (to be able to animate them!) which takes extremely long for the full data set
                    List(supportedBanksSearchResults.prefix(100), id: \.self) { bank in
                        BankInfoListItem(bank) {
                            self.bankHasBeenSelected(bank)
                        }
                    }
                }
            }
            
            if unsupportedBanksSearchResults.isNotEmpty {
                Section(header: Text("Unsupported banks")) {
                    // TODO: showing only the first 100 items is a workaround as SwiftUI tries to compare the two lists (to be able to animate them!) which takes extremely long for the full data set
                    List(unsupportedBanksSearchResults.prefix(100), id: \.self) { bank in
                        BankInfoListItem(bank) {
                            self.showBankIsNotSupportedMessage(bank)
                        }
                    }
                }
            }
        }
        .alert(message: $errorMessage)
        .fixKeyboardCoversLowerPart()
        .executeMutatingMethod {
            if isInitialized == false {
                isInitialized = true
                
                findBanks("")
            }
        }
        .showNavigationBarTitle("Select Bank Dialog Title")
    }
    
    
    private func findBanks(_ query: String) {
        let searchResult = presenter.searchBanksByNameBankCodeOrCity(query: query)
        
        supportedBanksSearchResults = searchResult.filter { $0.supportsFinTs3_0 }
        unsupportedBanksSearchResults = searchResult.filter { $0.supportsFinTs3_0 == false }
    }
    
    private func bankHasBeenSelected(_ bank: BankInfo) {
        self.selectedBank = bank

        presentation.wrappedValue.dismiss()
    }
    
    private func showBankIsNotSupportedMessage(_ bank: BankInfo) {
        self.errorMessage = Message(title: Text("\(bank.name) does not support FinTS 3.0"), message: Text("\(bank.name) does not support FinTS 3.0."))
    }
    
}


struct FindBankDialog_Previews: PreviewProvider {
    
    static var previews: some View {
        SelectBankDialog(.constant(nil))
    }
    
}
