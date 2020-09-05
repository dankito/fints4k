import SwiftUI
import BankingUiSwift


struct SelectBankDialog: View {
    
    @Environment(\.presentationMode) var presentation


    private let bankFinder = InMemoryBankFinder()
    
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
    
    @State private var supportedBanksSearchResults: [BankInfo]
    
    @State private var unsupportedBanksSearchResults: [BankInfo]

    
    @State private var errorMessage: Message? = nil
    
    
    init(_ selectedBank: Binding<BankInfo?>) {
        self._selectedBank = selectedBank
        
        bankFinder.preloadBankList()
        
        let allBanks = bankFinder.getBankList()
        _supportedBanksSearchResults = State(initialValue:allBanks.filter { $0.supportsFinTs3_0 })
        _unsupportedBanksSearchResults = State(initialValue:allBanks.filter { $0.supportsFinTs3_0 == false })
    }
    
    
    var body: some View {
        Form {
            Section {
                VStack {
                    UIKitSearchBar(text: searchTextBinding, placeholder: "Bank code, bank name or city", focusOnStart: true)
                    
                    HStack {
                        Text("Search by bank code, bank name or city")
                            .font(.caption)
                            .styleAsDetail()
                        
                        Spacer()
                    }
                    .padding(.leading, 10)
                }
            }
            
            if supportedBanksSearchResults.isEmpty {
                Text("No supported banks found")
                    .detailForegroundColor()
                    .alignVertically(.center)
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
        .alert(item: $errorMessage) { message in
            Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
        }
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle("Select Bank Dialog Title")
    }
    
    
    private func findBanks(_ query: String) {
        let searchResult = self.bankFinder.findBankByNameBankCodeOrCity(query: query)
        
        supportedBanksSearchResults = searchResult.filter { $0.supportsFinTs3_0 }
        unsupportedBanksSearchResults = searchResult.filter { $0.supportsFinTs3_0 == false }
    }
    
    private func bankHasBeenSelected(_ bank: BankInfo) {
        self.selectedBank = bank

        presentation.wrappedValue.dismiss()
    }
    
    private func showBankIsNotSupportedMessage(_ bank: BankInfo) {
        self.errorMessage = Message(title: Text("\(bank.name) does not support FinTS 3.0"), message: Text("Only banks supporting FinTS 3.0 can be used in this app."))
    }
    
}


struct FindBankDialog_Previews: PreviewProvider {
    
    static var previews: some View {
        SelectBankDialog(.constant(nil))
    }
    
}
