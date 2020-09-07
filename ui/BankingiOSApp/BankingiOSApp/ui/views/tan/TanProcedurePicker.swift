import SwiftUI
import BankingUiSwift


struct TanProcedurePicker: View {
    
    private let bank: Customer
    
    private let selectedTanProcedureChanged: (TanProcedure) -> Void
    
    
    private var customersTanProcedures: [TanProcedure] = []
    
    private let initiallySelectedTanProcedure: TanProcedure?
    
    @State private var selectedTanProcedureIndex: Int
    
    private var selectedTanProcedureIndexBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedTanProcedureIndex },
            set: { newValue in
                if (self.selectedTanProcedureIndex != newValue || self.initiallySelectedTanProcedure == nil) { // only if TAN procedure has really changed
                    self.selectedTanProcedureIndex = newValue
                    self.dispatchSelectedTanProcedureChanged(self.customersTanProcedures[newValue])
                }
        })
    }
    
    
    init(_ bank: Customer, _ initiallySelectedTanProcedure: TanProcedure? = nil, selectedTanProcedureChanged: @escaping (TanProcedure) -> Void) {
        self.bank = bank
        
        self.selectedTanProcedureChanged = selectedTanProcedureChanged
        
        
        self.customersTanProcedures = bank.supportedTanProcedures.filter( {$0.type != .chiptanusb } ) // USB tan generators are not supported on iOS
        
        self.initiallySelectedTanProcedure = initiallySelectedTanProcedure ?? bank.selectedTanProcedure
        let initiallySelectedTanProcedureType = self.initiallySelectedTanProcedure?.type

        _selectedTanProcedureIndex = State(initialValue: customersTanProcedures.firstIndex(where: { $0.type == initiallySelectedTanProcedureType } )
            ?? bank.supportedTanProcedures.firstIndex(where: { $0.type != .chiptanmanuell && $0.type != .chiptanusb } )
            ?? 0)
    }
    

    var body: some View {
        Picker("TAN procedure", selection: selectedTanProcedureIndexBinding) {
            ForEach(0 ..< self.customersTanProcedures.count) { index in
                Text(self.customersTanProcedures[index].displayName)
            }
        }
    }
    
    
    private func dispatchSelectedTanProcedureChanged(_ selectedTanProcedure: TanProcedure) {
        // do async as at this point Picker dialog gets dismissed -> this EnterTanDialog would never get dismissed (and dismiss has to be called before callback.changeTanProcedure())
        DispatchQueue.main.async {
            self.selectedTanProcedureChanged(selectedTanProcedure)
        }
    }

}


struct TanProcedurePicker_Previews: PreviewProvider {

    static var previews: some View {
        TanProcedurePicker(previewBanks[0]) { _ in }
    }

}
