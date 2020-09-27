import SwiftUI
import BankingUiSwift
import Combine


struct TransferMoneyDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private var showAccounts = false
    
    private var accountsSupportingTransferringMoney: [IBankAccount] = []
    
    @State private var selectedAccountIndex = 0
    
    @State private var recipientName: String = ""
    @State private var isValidRecipientNameEntered = false
    @State private var recipientNameValidationResult: ValidationResult? = nil
    
    @State private var showRecipientAutocompleteList = false
    @State private var recipientSearchResults = [TransactionParty]()
    
    @State private var recipientIban: String = ""
    @State private var isValidRecipientIbanEntered = false
    @State private var recipientIbanValidationResult: ValidationResult? = nil
    
    @State private var recipientBic: String = ""
    @State private var isValidRecipientBicEntered = false
    @State private var recipientBicValidationResult: ValidationResult? = nil
    
    @State private var recipientBankInfo: String? = nil
    
    @State private var amount = ""
    @State private var isValidAmountEntered = false
    @State private var amountValidationResult: ValidationResult? = nil
    
    @State private var reference: String = ""
    @State private var isValidReferenceEntered = true
    @State private var referenceValidationResult: ValidationResult? = nil
    
    @State private var validateDataWhenShowingDialog = false
    
    @State private var realTimeTransfer = false
    
    @State private var isTransferringMoney = false
    
    @State private var transferMoneyResponseMessage: Message? = nil
    
    private let inputValidator = InputValidator()
    
    @State private var didJustCorrectEnteredValue = false
    
    @State private var doNotDoAnyChangesToUiAnymore = false
    
    
    private var account: IBankAccount? {
        if (self.selectedAccountIndex < self.accountsSupportingTransferringMoney.count) {
            return self.accountsSupportingTransferringMoney[selectedAccountIndex]
        }

        return self.accountsSupportingTransferringMoney.first
    }

    private var supportsRealTimeTransfer: Bool {
        return self.account?.supportsRealTimeTransfer ?? false
    }
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Inject private var persistence: CoreDataBankingPersistence
    
    
    init() {
        self.accountsSupportingTransferringMoney = self.presenter.accountsSupportingTransferringMoneySortedByDisplayIndex
        
        self.showAccounts = self.accountsSupportingTransferringMoney.count > 1
    }
    
    init(preselectedValues: TransferMoneyData) {
        self.init()
        
        let preselectedBankAccount = preselectedValues.account
        self._selectedAccountIndex = State(initialValue: accountsSupportingTransferringMoney.firstIndex(where: { account in account == preselectedBankAccount }) ?? 0)
        
        self._recipientName = State(initialValue: preselectedValues.recipientName)
        self._recipientBic = State(initialValue: preselectedValues.recipientBankCode)
        self._recipientIban = State(initialValue: preselectedValues.recipientAccountId)
        
        if recipientBic.isBlank && recipientIban.isNotBlank {
            tryToGetBicFromIban(recipientIban)
        }
        
        self._reference = State(initialValue: preselectedValues.reference)
        
        if preselectedValues.amount.decimal != NSDecimalNumber.zero {
            self._amount = State(initialValue: preselectedValues.amount.format(countDecimalPlaces: 2))
        }
        
        if preselectedBankAccount.supportsRealTimeTransfer {
            self._realTimeTransfer = State(initialValue: preselectedValues.realTimeTransfer)
        }
        
        _validateDataWhenShowingDialog = State(initialValue: true)
    }
    
    
    var body: some View {
        Form {
            if showAccounts {
                Section {
                    Picker("Account", selection: $selectedAccountIndex) {
                        ForEach(0 ..< self.accountsSupportingTransferringMoney.count) { accountIndex in
                            IconedTitleView(self.accountsSupportingTransferringMoney[accountIndex])
                        }
                    }
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Recipient Name", text: $recipientName, focusOnStart: true, focusNextTextFieldOnReturnKeyPress: true,
                                       isFocusedChanged: recipientNameIsFocusedChanged, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: enteredRecipientNameChanged)
                    .padding(.bottom, 0)

                recipientNameValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                if self.showRecipientAutocompleteList {
                    List(self.recipientSearchResults) { recipient in
                        RecipientListItem(recipient: recipient)
                            .onTapGesture { self.recipientSelected(recipient) }
                    }
                }
                
                LabelledUIKitTextField(label: "Recipient IBAN", text: $recipientIban, autocapitalizationType: .allCharacters, focusNextTextFieldOnReturnKeyPress: true, isFocusedChanged: validateRecipientIbanOnFocusLost,
                                       actionOnReturnKeyPress: handleReturnKeyPress, textChanged: enteredRecipientIbanChanged)

                recipientIbanValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                recipientBankInfo.map {
                    InfoLabel($0)
                    .font(.caption)
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Amount", text: $amount, keyboardType: .decimalPad, addDoneButton: true,
                                       focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: checkAndValidateEnteredAmount)

                amountValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
            }
            
            Section {
                VStack(alignment: .leading) {
                    HStack {
                        Text("Reference")
                        
                        Spacer()
                    }
                    
                    UIKitTextField("Enter reference", text: $reference, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: validateReference)
                }

                referenceValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
            }
            
            if supportsRealTimeTransfer {
                Section {
                    VStack {
                        Toggle(isOn: $realTimeTransfer) {
                            HStack {
                                Text("Real-time transfer")
                                    .lineLimit(1)
                                
                                if realTimeTransfer {
                                    RealTimeTransferInfoView()
                                }
                            }
                        }
                    }
                }
            }
            
            Section {
                HStack {
                    Spacer()
                    
                    Button("Transfer Money") { self.transferMoney() }
                        .disabled( !self.isRequiredDataEntered() || isTransferringMoney)
                    
                    Spacer()
                }
                .overlay(UIKitActivityIndicator($isTransferringMoney), alignment: .leading)
            }
        }
        .onAppear {
            // if preselectedValues are set we have to call the validate() methods manually - and after init() method (don't know why)
            if self.validateDataWhenShowingDialog {
                self.validateDataWhenShowingDialog = false
                self.validateAllFields()
                
                if self.recipientBankInfo == nil {
                    self.showRecipientBankInfo(self.recipientBic, "")
                }
            }
        }
        .alert(message: $transferMoneyResponseMessage)
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle("Transfer Money Dialog Title")
        .customNavigationBarBackButton(cancelPressed)
    }
    
    
    private func handleReturnKeyPress() -> Bool {
        if self.isRequiredDataEntered() && isTransferringMoney == false {
            self.transferMoney()
            
            return true
        }
        
        return false
    }
    
    
    private func validateAllFields() {
        self.validateRecipientNameOnFocusLost()
        self.validateRecipientIbanOnFocusLost()
        self.validateRecipientBic()
        self.validateAmount()
        self.validateReference()
    }
    
    
    private func recipientNameIsFocusedChanged(_ isFocused: Bool) {
        if doNotDoAnyChangesToUiAnymore {
            return
        }
        
        if isFocused == false {
            validateRecipientNameOnFocusLost()
            
            self.showRecipientAutocompleteList = false
        }
        else {
            self.showRecipientAutocompleteList = self.recipientSearchResults.isNotEmpty
        }
    }
    
    private func validateRecipientNameOnFocusLost() {
        validateField($recipientName, $recipientNameValidationResult, $isValidRecipientNameEntered) {
            inputValidator.validateRecipientName(recipientNameToTest: recipientName)
        }
    }
    
    private func enteredRecipientNameChanged(enteredRecipientName: String) {
        validateField($recipientName, $recipientNameValidationResult, $isValidRecipientNameEntered) {
            inputValidator.validateRecipientNameWhileTyping(recipientNameToTest: recipientName)
        }

        searchRecipients(recipientName)
    }

    private func searchRecipients(_ searchText: String) {
        // TODO: why doesn't it work to search on background thread?
        self.recipientSearchResults = self.presenter.findRecipientsForName(name: searchText)

        self.showRecipientAutocompleteList = self.recipientSearchResults.isNotEmpty
    }

    private func recipientSelected(_ recipient: TransactionParty) {
        self.recipientName = recipient.name
        self.recipientIban = recipient.iban ?? self.recipientIban
        self.recipientBic = recipient.bic ?? self.recipientBic
        
        tryToGetBicFromIban(self.recipientIban)
        
        validateAllFields()

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            self.showRecipientAutocompleteList = false
        }
    }
    
    
    private func enteredRecipientIbanChanged(_ enteredIban: String) {
        validateField($recipientIban, $recipientIbanValidationResult, $isValidRecipientIbanEntered) { inputValidator.validateIbanWhileTyping(ibanToTest: enteredIban) }
         
        tryToGetBicFromIban(enteredIban)
    }
    
    private func validateRecipientIbanOnFocusLost(_ isFocused: Bool) {
        if doNotDoAnyChangesToUiAnymore {
            return
        }
        
        if isFocused == false {
            validateRecipientIbanOnFocusLost()
        }
    }
    
    private func validateRecipientIbanOnFocusLost() {
        validateField($recipientIban, $recipientIbanValidationResult, $isValidRecipientIbanEntered) { inputValidator.validateIban(ibanToTest: recipientIban) }
    }
    
    private func tryToGetBicFromIban(_ enteredIban: String) {
        let foundBank = presenter.findUniqueBankForIban(iban: enteredIban)
        
        if let foundBank = foundBank {
            self.recipientBic = foundBank.bic
            showRecipientBankInfo(foundBank.bic, foundBank.name)
        }
        else {
            self.recipientBic = ""
            
            if enteredIban.count >= InputValidator.Companion().MinimumLengthToDetermineBicFromIban {
                self.recipientBankInfo = "No BIC found for bank code \(enteredIban[4..<Int(InputValidator.Companion().MinimumLengthToDetermineBicFromIban)])"
            }
            else {
                self.recipientBankInfo = nil
            }
        }

        validateRecipientBic()
    }

    private func validateRecipientBic() {
        self.isValidRecipientBicEntered = inputValidator.validateBic(bicToTest: recipientBic).validationSuccessful
    }
    
    private func showRecipientBankInfo(_ bic: String, _ bankName: String) {
        self.recipientBankInfo = "BIC: \(bic), \(bankName)"
    }
    
    
    private func checkAndValidateEnteredAmount(_ enteredAmount: String) {
        // TODO: implement DecimalTextField / NumericTextField
        let filtered = enteredAmount.filter { "0123456789,".contains($0) }
        if filtered != enteredAmount {
            self.amount = filtered
            
            return // don't validate field after non decimal character has been entered
        }
        
        validateAmount()
    }
    
    private func validateAmount() {
        if amount.isNotBlank {
            validateField($amount, $amountValidationResult, $isValidAmountEntered) { inputValidator.validateAmount(enteredAmountString: self.amount) }
        }
        else {
            isValidAmountEntered = false
            amountValidationResult = nil
        }
    }
        
        
    private func validateReference(enteredReference: String) {
        validateReference()
    }
           
   private func validateReference() {
        validateField($reference, $referenceValidationResult, $isValidReferenceEntered) { inputValidator.validateReference(referenceToTest: self.reference) }
   }
    
    private func validateField(_ newValue: Binding<String>, _ validationResult: Binding<ValidationResult?>, _ isValidValueEntered: Binding<Bool>, _ validateValue: () -> ValidationResult) {
        
        if (didJustCorrectEnteredValue == false) {
            let fieldValidationResult = validateValue()
            
            isValidValueEntered.wrappedValue = fieldValidationResult.validationSuccessfulOrCouldCorrectString
            
            validationResult.wrappedValue = fieldValidationResult.didCorrectString || fieldValidationResult.validationSuccessful == false ? fieldValidationResult :  nil
            
            if (fieldValidationResult.didCorrectString) {
                didJustCorrectEnteredValue = true
                
                newValue.wrappedValue = fieldValidationResult.correctedInputString
            }
        }
        else {
            didJustCorrectEnteredValue = false
        }
    }
    
    
    private func isRequiredDataEntered() -> Bool {
        return account != nil
                && isValidRecipientNameEntered
                && isValidRecipientIbanEntered
                && isValidRecipientBicEntered
                && isValidAmountEntered
                && isValidReferenceEntered
    }
    
    private func transferMoney() {
        if let amount = inputValidator.convertAmountString(enteredAmountString: self.amount) {
            isTransferringMoney = true
            UIApplication.hideKeyboard()
            
            let data = TransferMoneyData(account: account!, recipientName: recipientName, recipientAccountId: recipientIban, recipientBankCode: recipientBic, amount: amount, reference: reference, realTimeTransfer: realTimeTransfer)
            
            presenter.transferMoneyAsync(data: data) { response in
                self.handleTransferMoneyResponse(data, response)
            }
        }
    }
    
    private func handleTransferMoneyResponse(_ data: TransferMoneyData, _ response: BankingClientResponse) {
        isTransferringMoney = false
        
        if (response.successful) {
            self.transferMoneyResponseMessage = Message(message: Text("Successfully transferred \(data.amount) \("€") to \(data.recipientName)."), primaryButton: .ok {
                self.closeDialog()
            })
        }
        else if response.userCancelledAction == false {
            self.transferMoneyResponseMessage = Message(message: Text("Could not transfer \(data.amount) \("€") to \(data.recipientName). Error: \(response.errorToShowToUser ?? "")."))
        }
    }
    
    
    private func cancelPressed() {
        // ugly, i know. iOS 14 crashes when after pressing cancel e.g. due to validation count cells changes -> don't do any changes or validation anymore after cancel navigation bar button has been pressed
        doNotDoAnyChangesToUiAnymore = true
        
        closeDialog()
    }
    
    private func closeDialog() {
        presentation.wrappedValue.dismiss()
    }
    
}

struct TransferMoneyDialog_Previews: PreviewProvider {
    static var previews: some View {
        TransferMoneyDialog()
            .environment(\.locale, .init(identifier: "de"))
    }
}
