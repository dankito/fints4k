import SwiftUI
import BankingUiSwift


struct EnterTanDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private var state: EnterTanState
    
    private var tanChallenge: TanChallenge
    
    private var customer: Customer
    
    private var customersTanProcedures: [TanProcedure] = []
    
    @State private var selectedTanProcedureIndex: Int
    
    private var selectedTanProcedureIndexBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedTanProcedureIndex },
            set: {
                if (self.selectedTanProcedureIndex != $0) { // only if TAN procedure has really changed
                    self.selectedTanProcedureIndex = $0
                    self.selectedTanProcedureChanged(self.customersTanProcedures[$0])
                }
        })
    }
    
    @State private var selectedTanMediumIndex = 0
    private var customersTanMedia: [TanMedium] = []
    
    private var showSelectTanMediumView = false
    
    private var showFlickerCodeTanView = false
    
    private var showImageTanView = false
    
    @State private var enteredTan = ""
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ state: EnterTanState) {
        self.state = state
        
        self.tanChallenge = state.tanChallenge
        self.customer = state.customer
        
        self.customersTanProcedures = customer.supportedTanProcedures.filter( {$0.type != .chiptanusb } ) // USB tan generators are not supported on iOS

        _selectedTanProcedureIndex = State(initialValue: customersTanProcedures.firstIndex(where: { $0.type == state.tanChallenge.tanProcedure.type } )
            ?? state.customer.supportedTanProcedures.firstIndex(where: { $0.type != .chiptanmanuell && $0.type != .chiptanusb } )
            ?? 0)
        
        self.customersTanMedia = customer.tanMediaSorted
        
        self.showSelectTanMediumView = true // TODO: use isOpticalTanProcedure && tanMedia.count > 1
        
        self.showFlickerCodeTanView = tanChallenge is FlickerCodeTanChallenge
        self.showImageTanView = tanChallenge is ImageTanChallenge
    }
    
    
    var body: some View {
        Form {
            Section {
                Picker("TAN procedure", selection: selectedTanProcedureIndexBinding) {
                    ForEach(0 ..< self.customersTanProcedures.count) { index in
                        Text(self.customersTanProcedures[index].displayName)
                    }
                }
                
                if showSelectTanMediumView {
                    Picker("TAN medium", selection: $selectedTanMediumIndex) {
                        ForEach(0 ..< self.customersTanMedia.count) { index in
                            Text(self.customersTanMedia[index].displayName)
                        }
                    }
                }
            }
            
            if showFlickerCodeTanView {
                Text("Entschuldigen Sie, aber die Darstellung von Flicker Codes wird gegenwärtig noch nicht unterstützt (fauler Programmierer). Bitte wählen Sie ein anderes TAN Verfahren, im Notfall chipTAN manuell.")
            }
            
            if showImageTanView {
                ImageTanView(self.tanChallenge as! ImageTanChallenge)
            }
            
            VStack {
                HStack {
                    Text("TAN hint from your bank:")
                    Spacer()
                }
                
                HStack {
                    Text(tanChallenge.messageToShowToUser)
                    Spacer()
                }
                .padding(.top, 6)
            }
            .padding(.vertical)
            
            Section {
                TextField("Enter TAN:", text: $enteredTan)
            }
            
            Section {
                HStack {
                    Spacer()
                    Button(action: { self.enteringTanDone() },
                           label: { Text("OK") })
                        .disabled(self.enteredTan.isEmpty)
                    Spacer()
                }
            }
        }
        .showNavigationBarTitle("Enter TAN Dialog Title")
        .customNavigationBarBackButton {
            self.sendEnterTanResult(EnterTanResult.Companion().userDidNotEnterTan())
        }
    }
    
    
    private func selectedTanProcedureChanged(_ changeTanProcedureTo: TanProcedure) {
        // do async as at this point Picker dialog gets dismissed -> this EnterTanDialog would never get dismissed (and dismiss has to be called before callback.changeTanProcedure())
        DispatchQueue.main.async {
            self.dismissDialog()
            
            self.state.callback(EnterTanResult.Companion().userAsksToChangeTanProcedure(changeTanProcedureTo: changeTanProcedureTo))
        }
    }
    
    
    private func enteringTanDone() {
        let companion = EnterTanResult.Companion()
        let result = enteredTan.isEmpty ? companion.userDidNotEnterTan() : companion.userEnteredTan(enteredTan: enteredTan)
        
        sendEnterTanResult(result)
    }
    
    private func sendEnterTanResult(_ result: EnterTanResult) {
        state.callback(result)
        
        // TODO: if a TAN has been entered, check result if user has made a mistake and has to re-enter TAN
        dismissDialog()
    }
    
    private func dismissDialog() {
        presentation.wrappedValue.dismiss()
    }
    
}


struct EnterTanDialog_Previews: PreviewProvider {
    static var previews: some View {
        let customer = Customer(bankCode: "", customerId: "", password: "", finTsServerAddress: "")
        customer.supportedTanProcedures = [
            TanProcedure(displayName: "chipTAN optisch", type: .chiptanflickercode, bankInternalProcedureCode: ""),
            TanProcedure(displayName: "chipTAN QR", type: .chiptanqrcode, bankInternalProcedureCode: ""),
            TanProcedure(displayName: "Secure Super Duper Plus", type: .apptan, bankInternalProcedureCode: "")
        ]
        
        customer.tanMedia = [
            TanMedium(displayName: "EC-Karte mit Nummer 12345678", status: .available),
            TanMedium(displayName: "Handy mit Nummer 0170 / 12345678", status: .available)
        ]
        
        let tanChallenge = TanChallenge(messageToShowToUser: "Hier ist eine Nachricht deiner Bank, die dir die Welt erklaert", tanProcedure: customer.supportedTanProcedures[0])
        
        let enterTanState = EnterTanState(customer, tanChallenge, { result in })
        
        return EnterTanDialog(enterTanState)
    }
}