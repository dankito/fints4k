import SwiftUI
import BankingUiSwift


struct TanMethodPicker: View {
    
    private let bank: ICustomer
    
    private let selectedTanMethodChanged: (TanMethod) -> Void
    
    
    private var customersTanMethods: [TanMethod] = []
    
    private let initiallySelectedTanMethod: TanMethod?
    
    @State private var selectedTanMethodIndex: Int
    
    private var selectedTanMethodIndexBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedTanMethodIndex },
            set: { newValue in
                if (self.selectedTanMethodIndex != newValue || self.initiallySelectedTanMethod == nil) { // only if TAN method has really changed
                    self.selectedTanMethodIndex = newValue
                    self.dispatchSelectedTanMethodChanged(self.customersTanMethods[newValue])
                }
        })
    }
    
    
    init(_ bank: ICustomer, _ initiallySelectedTanMethod: TanMethod? = nil, selectedTanMethodChanged: @escaping (TanMethod) -> Void) {
        self.bank = bank
        
        self.selectedTanMethodChanged = selectedTanMethodChanged
        
        
        self.customersTanMethods = bank.supportedTanMethods.filter( {$0.type != .chiptanusb } ) // USB tan generators are not supported on iOS
        
        self.initiallySelectedTanMethod = initiallySelectedTanMethod ?? bank.selectedTanMethod
        let initiallySelectedTanMethodType = self.initiallySelectedTanMethod?.type

        _selectedTanMethodIndex = State(initialValue: customersTanMethods.firstIndex(where: { $0.type == initiallySelectedTanMethodType } )
            ?? bank.supportedTanMethods.firstIndex(where: { $0.type != .chiptanmanuell && $0.type != .chiptanusb } )
            ?? 0)
    }
    

    var body: some View {
        Picker("TAN method", selection: selectedTanMethodIndexBinding) {
            ForEach(0 ..< self.customersTanMethods.count) { index in
                Text(self.customersTanMethods[index].displayName)
            }
        }
    }
    
    
    private func dispatchSelectedTanMethodChanged(_ selectedTanMethod: TanMethod) {
        // do async as at this point Picker dialog gets dismissed -> this EnterTanDialog would never get dismissed (and dismiss has to be called before callback.changeTanMethod())
        DispatchQueue.main.async {
            self.selectedTanMethodChanged(selectedTanMethod)
        }
    }

}


struct TanMethodPicker_Previews: PreviewProvider {

    static var previews: some View {
        TanMethodPicker(previewBanks[0]) { _ in }
    }

}
