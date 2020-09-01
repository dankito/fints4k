import SwiftUI
import BankingUiSwift


struct EnterTanDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private var state: EnterTanState
    
    private var tanChallenge: TanChallenge
    
    private var customer: Customer
    
    private var customersTanMedia: [TanMedium] = []
    
    @State private var selectedTanMediumIndex = 0
    
    private var selectedTanMediumIndexBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedTanMediumIndex },
            set: {
                if (self.selectedTanMediumIndex != $0) { // only if TAN media has really changed
                    self.selectedTanMediumIndex = $0
                    self.selectedTanMediumChanged(self.customersTanMedia[$0])
                }
        })
    }
    
    
    private var showSelectTanMediumView = false
    
    private let flickerCodeTanChallenge: FlickerCodeTanChallenge?
    
    private let imageTanChallenge: ImageTanChallenge?
    
    private let messageToShowToUser: String
    
    @State private var enteredTan = ""

    
    @State private var errorMessage: Message? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ state: EnterTanState) {
        self.state = state
        
        self.tanChallenge = state.tanChallenge
        self.customer = state.customer
        
        self.customersTanMedia = customer.tanMediaSorted
        
        self.showSelectTanMediumView = self.customersTanMedia.count > 1 // TODO: use isOpticalTanProcedure && tanMedia.count > 1
        
        self.flickerCodeTanChallenge = tanChallenge as? FlickerCodeTanChallenge
        self.imageTanChallenge = tanChallenge as? ImageTanChallenge
        
        self.messageToShowToUser = tanChallenge.messageToShowToUser.htmlToString // parse in init() calling this method in body { } crashes application
        
        if let decodingError = (tanChallenge as? FlickerCodeTanChallenge)?.flickerCode.decodingError {
            showDecodingTanChallengeFailedErrorDelayed(decodingError)
        }
        if let decodingError = (tanChallenge as? ImageTanChallenge)?.image.decodingError {
            showDecodingTanChallengeFailedErrorDelayed(decodingError)
        }
    }
    
    
    var body: some View {
        Form {
            Section {
                TanProcedurePicker(customer, state.tanChallenge.tanProcedure) { selectedTanProcedure in
                    self.selectedTanProcedureChanged(selectedTanProcedure)
                }
                
                if showSelectTanMediumView {
                    Picker("TAN medium", selection: selectedTanMediumIndexBinding) {
                        ForEach(0 ..< self.customersTanMedia.count) { index in
                            Text(self.customersTanMedia[index].displayName)
                        }
                    }
                }
            }
            
            flickerCodeTanChallenge.map { flickerCodeTanChallenge in
                FlickerCodeTanView(flickerCodeTanChallenge)
            }
            
            imageTanChallenge.map { imageTanChallenge in
                ImageTanView(imageTanChallenge)
            }
            
            VStack {
                HStack {
                    Text("TAN hint from your bank:")
                    Spacer()
                }
                
                HStack {
                    CollapsibleText(messageToShowToUser)
                    Spacer()
                }
                .padding(.top, 6)
            }
            .padding(.vertical, 2)
            
            Section {
                LabelledUIKitTextField(label: "Enter TAN:", text: $enteredTan, keyboardType: tanChallenge.tanProcedure.isNumericTan ? .numberPad : .default, autocapitalizationType: .none, actionOnReturnKeyPress: {
                    if self.isRequiredDataEntered() {
                        self.enteringTanDone()
                        return true
                    }
                    
                    return false
               })
            }
            
            Section {
                HStack {
                    Spacer()
                    Button(action: { self.enteringTanDone() },
                           label: { Text("OK") })
                        .disabled( !self.isRequiredDataEntered())
                    Spacer()
                }
            }
        }
        .alert(item: $errorMessage) { message in
            Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
        }
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle("Enter TAN Dialog Title")
        .customNavigationBarBackButton {
            self.sendEnterTanResult(EnterTanResult.Companion().userDidNotEnterTan())
        }
    }
    
    
    private func isRequiredDataEntered() -> Bool {
        return self.enteredTan.isNotBlank
    }
    
    private func selectedTanProcedureChanged(_ changeTanProcedureTo: TanProcedure) {
        // do async as at this point Picker dialog gets dismissed -> this EnterTanDialog would never get dismissed (and dismiss has to be called before callback.changeTanProcedure())
        DispatchQueue.main.async {
            self.dismissDialog()
            
            self.state.callback(EnterTanResult.Companion().userAsksToChangeTanProcedure(changeTanProcedureTo: changeTanProcedureTo))
        }
    }
    
    private func selectedTanMediumChanged(_ changeTanMediumTo: TanMedium) {
        if (changeTanMediumTo.status == .used) { // TAN medium already in use, no need to activate it
            return
        }
        
        // do async as at this point Picker dialog gets dismissed -> this EnterTanDialog would never get dismissed (and dismiss has to be called before callback.changeTanMedium())
        DispatchQueue.main.async {
            self.dismissDialog()
            
            self.state.callback(EnterTanResult.Companion().userAsksToChangeTanMedium(changeTanMediumTo: changeTanMediumTo) { changeTanMediumResponse in
                self.handleChangeTanMediumResponse(changeTanMediumTo, changeTanMediumResponse)
            })
        }
    }
    
    private func handleChangeTanMediumResponse(_ newTanMedium: TanMedium, _ changeTanMediumResponse: BankingClientResponse) {
        if (changeTanMediumResponse.isSuccessful) {
            self.errorMessage = Message(title: Text("TAN medium change"), message: Text("TAN medium successfully changed to \(newTanMedium.displayName)."))
        }
        else {
            self.errorMessage = Message(title: Text("TAN medium change"), message: Text("Could not change TAN medium to \(newTanMedium.displayName). Error: \(changeTanMediumResponse.errorToShowToUser ?? "")."))
        }
    }
    
    /**
        This method gets called right on start up before dialog is shown -> Alert would get displayed before dialog and therefore covered by dialog -> delay displaying alert.
     */
    private func showDecodingTanChallengeFailedErrorDelayed(_ error: KotlinException?) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.showDecodingTanChallengeFailedError(error)
        }
    }
    
    private func showDecodingTanChallengeFailedError(_ error: KotlinException?) {
        if let errorMessage = error?.message {
            self.errorMessage = Message(title: "Decoding error", message: "Could not decode TAN challenge. Error: \(errorMessage).", primaryButton: .ok())
        }
    }
    
    
    private func enteringTanDone() {
        let companion = EnterTanResult.Companion()
        let result = enteredTan.isBlank ? companion.userDidNotEnterTan() : companion.userEnteredTan(enteredTan: enteredTan)
        
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
        customer.supportedTanProcedures = previewTanProcedures
        
        customer.tanMedia = previewTanMedia
        
        let tanChallenge = previewTanChallenge
        
        let enterTanState = EnterTanState(customer, tanChallenge, { result in })
        
        return EnterTanDialog(enterTanState)
    }
}
