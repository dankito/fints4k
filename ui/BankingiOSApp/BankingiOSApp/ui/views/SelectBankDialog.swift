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
    
    @State private var searchResult: [BankInfo]
    
    
    init(_ selectedBank: Binding<BankInfo?>) {
        self._selectedBank = selectedBank
        
        bankFinder.preloadBankList()
        
        _searchResult = State(initialValue: bankFinder.getBankList())
    }
    
    
    var body: some View {
        Form {
            Section {
                UIKitSearchBar(text: searchTextBinding) // TODO: try to get rid of the two horizontal lines
            }
            
            Section {
                // TODO: showing only the first 100 items is a workaround as SwiftUI tries to compare the two lists (to be able to animate them!) which takes extremely long for the full data set
                List(searchResult.prefix(100), id: \.self) { bank in
                    BankInfoListItem(bank: bank)
                        .onTapGesture {
                            self.handleSelectedBank(bank)
                    }
                }
            }
        }
        .showNavigationBarTitle("Select Bank Dialog Title")
    }
    
    
    private func findBanks(_ query: String) {
        self.searchResult = self.bankFinder.findBankByNameBankCodeOrCity(query: query)
    }
    
    private func handleSelectedBank(_ bank: BankInfo) {
        self.selectedBank = bank

        presentation.wrappedValue.dismiss()
    }
    
}


struct FindBankDialog_Previews: PreviewProvider {
    
    static var previews: some View {
        SelectBankDialog(.constant(nil))
    }
    
}
