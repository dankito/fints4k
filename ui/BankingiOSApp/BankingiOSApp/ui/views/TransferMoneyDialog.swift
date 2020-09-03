import SwiftUI
import BankingUiSwift
import Combine


struct TransferMoneyDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private var showAccounts = false
    
    private var accountsSupportingTransferringMoney: [BankAccount] = []
    
    @State private var selectedAccountIndex = 0
    
    @State private var remitteeName: String = ""
    @State private var isValidRemitteeNameEntered = false
    @State private var remitteeNameValidationResult: ValidationResult? = nil
    
    @State private var showRemitteeAutocompleteList = false
    @State private var remitteeSearchResults = [Remittee]()
    
    @State private var remitteeIban: String = ""
    @State private var isValidRemitteeIbanEntered = false
    @State private var remitteeIbanValidationResult: ValidationResult? = nil
    
    @State private var remitteeBic: String = ""
    @State private var isValidRemitteeBicEntered = false
    @State private var remitteeBicValidationResult: ValidationResult? = nil
    
    @State private var remitteeBankInfo: String? = nil
    
    @State private var amount = ""
    @State private var isValidAmountEntered = false
    @State private var amountValidationResult: ValidationResult? = nil
    
    @State private var usage: String = ""
    @State private var isValidUsageEntered = true
    @State private var usageValidationResult: ValidationResult? = nil
    
    @State private var validateDataWhenShowingDialog = false
    
    @State private var instantPayment = false
    
    @State private var isTransferringMoney = false
    
    @State private var transferMoneyResponseMessage: Message? = nil
    
    private let inputValidator = InputValidator()
    
    @State private var didJustCorrectEnteredValue = false
    
    
    private var account: BankAccount? {
        if (self.selectedAccountIndex < self.accountsSupportingTransferringMoney.count) {
            return self.accountsSupportingTransferringMoney[selectedAccountIndex]
        }

        return self.accountsSupportingTransferringMoney.first
    }

    private var supportsInstantPayment: Bool {
        return self.account?.supportsInstantPaymentMoneyTransfer ?? false
    }
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    @Inject private var persistence: CoreDataBankingPersistence
    
    
    init() {
        self.accountsSupportingTransferringMoney = self.presenter.bankAccounts.filter({ $0.supportsTransferringMoney })
        
        self.showAccounts = self.accountsSupportingTransferringMoney.count > 1
    }
    
    init(preselectedBankAccount: BankAccount, preselectedValues: TransferMoneyData) {
        self.init()
        
        self._selectedAccountIndex = State(initialValue: accountsSupportingTransferringMoney.firstIndex(where: { account in account == preselectedBankAccount }) ?? 0)
        
        self._remitteeName = State(initialValue: preselectedValues.creditorName)
        self._remitteeBic = State(initialValue: preselectedValues.creditorBic)
        self._remitteeIban = State(initialValue: preselectedValues.creditorIban)
        
        if remitteeBic.isBlank && remitteeIban.isNotBlank {
            tryToGetBicFromIban(remitteeIban)
        }
        
        self._usage = State(initialValue: preselectedValues.usage)
        
        if preselectedValues.amount.decimal != NSDecimalNumber.zero {
            self._amount = State(initialValue: preselectedValues.amount.format(countDecimalPlaces: 2))
        }
        
        if preselectedBankAccount.supportsInstantPaymentMoneyTransfer {
            self._instantPayment = State(initialValue: preselectedValues.instantPayment)
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
                LabelledUIKitTextField(label: "Remittee Name", text: $remitteeName, focusOnStart: true, focusNextTextFieldOnReturnKeyPress: true,
                                       isFocusedChanged: remitteeNameisFocusedChanged, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: enteredRemitteeNameChanged)
                    .padding(.bottom, 0)

                remitteeNameValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                if self.showRemitteeAutocompleteList {
                    List(self.remitteeSearchResults) { remittee in
                        RemitteeListItem(remittee: remittee)
                            .onTapGesture { self.remitteeSelected(remittee) }
                    }
                }
                
                LabelledUIKitTextField(label: "Remittee IBAN", text: $remitteeIban, autocapitalizationType: .allCharacters, focusNextTextFieldOnReturnKeyPress: true, isFocusedChanged: validateRemitteeIbanOnFocusLost,
                                       actionOnReturnKeyPress: handleReturnKeyPress, textChanged: remitteeIbanisFocusedChanged)

                remitteeIbanValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                remitteeBankInfo.map {
                    InfoLabel($0)
                    .font(.caption)
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Amount", text: $amount, keyboardType: .decimalPad, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: checkAndValidateEnteredAmount)

                amountValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                LabelledUIKitTextField(label: "Usage", text: $usage, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: validateUsage)

                usageValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
            }
            
            if supportsInstantPayment {
                Section {
                    Toggle("Instant Payment", isOn: $instantPayment)
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
                
                if self.remitteeBankInfo == nil {
                    self.showRemitteeBankInfo(self.remitteeBic, "")
                }
            }
        }
        .alert(item: $transferMoneyResponseMessage) { message in
            if let secondaryButton = message.secondaryButton {
                return Alert(title: message.title, message: message.message, primaryButton: message.primaryButton, secondaryButton: secondaryButton)
            }
            else {
                return Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
            }
        }
        .fixKeyboardCoversLowerPart()
        .showNavigationBarTitle("Transfer Money Dialog Title")
    }
    
    
    private func handleReturnKeyPress() -> Bool {
        if self.isRequiredDataEntered() && isTransferringMoney == false {
            self.transferMoney()
            
            return true
        }
        
        return false
    }
    
    
    private func validateAllFields() {
        self.validateRemitteeNameOnFocusLost()
        self.validateRemitteeIbanOnFocusLost()
        self.validateRemitteeBic()
        self.validateAmount()
        self.validateUsage()
    }
    
    
    private func remitteeNameisFocusedChanged(_ isFocused: Bool) {
        if isFocused == false {
            validateRemitteeNameOnFocusLost()
            
            self.showRemitteeAutocompleteList = false
        }
        else {
            self.showRemitteeAutocompleteList = self.remitteeSearchResults.isNotEmpty
        }
    }
    
    private func validateRemitteeNameOnFocusLost() {
        validateField($remitteeName, $remitteeNameValidationResult, $isValidRemitteeNameEntered) {
            inputValidator.validateRemitteeName(remitteeNameToTest: remitteeName)
        }
    }
    
    private func enteredRemitteeNameChanged(enteredRemitteeName: String) {
        validateField($remitteeName, $remitteeNameValidationResult, $isValidRemitteeNameEntered) {
            inputValidator.validateRemitteeNameWhileTyping(remitteeNameToTest: remitteeName)
        }

        searchRemittees(remitteeName)
    }

    private func searchRemittees(_ searchText: String) {
        // TODO: why doesn't it work to search on background thread?
        self.remitteeSearchResults = self.presenter.findRemitteesForName(name: searchText)

        self.showRemitteeAutocompleteList = self.remitteeSearchResults.isNotEmpty
    }

    private func remitteeSelected(_ remittee: Remittee) {
        self.remitteeName = remittee.name
        self.remitteeIban = remittee.iban ?? self.remitteeIban
        self.remitteeBic = remittee.bic ?? self.remitteeBic
        
        tryToGetBicFromIban(self.remitteeIban)
        
        validateAllFields()

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            self.showRemitteeAutocompleteList = false
        }
    }
    
    
    private func remitteeIbanisFocusedChanged(_ enteredIban: String) {
        validateField($remitteeIban, $remitteeIbanValidationResult, $isValidRemitteeIbanEntered) { inputValidator.validateIbanWhileTyping(ibanToTest: enteredIban) }
         
        tryToGetBicFromIban(enteredIban)
    }
    
    private func validateRemitteeIbanOnFocusLost(_ isFocused: Bool) {
        if isFocused == false {
            validateRemitteeIbanOnFocusLost()
        }
    }
    
    private func validateRemitteeIbanOnFocusLost() {
        validateField($remitteeIban, $remitteeIbanValidationResult, $isValidRemitteeIbanEntered) { inputValidator.validateIban(ibanToTest: remitteeIban) }
    }
    
    private func tryToGetBicFromIban(_ enteredIban: String) {
        let foundBank = presenter.findUniqueBankForIban(iban: enteredIban)
        
        if let foundBank = foundBank {
            self.remitteeBic = foundBank.bic
            showRemitteeBankInfo(foundBank.bic, foundBank.name)
        }
        else {
            self.remitteeBic = ""
            
            if enteredIban.count >= InputValidator.Companion().MinimumLengthToDetermineBicFromIban {
                self.remitteeBankInfo = "No BIC found for bank code \(enteredIban[4..<Int(InputValidator.Companion().MinimumLengthToDetermineBicFromIban)])"
            }
            else {
                self.remitteeBankInfo = nil
            }
        }

        validateRemitteeBic()
    }

    private func validateRemitteeBic() {
        self.isValidRemitteeBicEntered = inputValidator.validateBic(bicToTest: remitteeBic).validationSuccessful
    }
    
    private func showRemitteeBankInfo(_ bic: String, _ bankName: String) {
        self.remitteeBankInfo = "BIC: \(bic), \(bankName)"
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
        
        
    private func validateUsage(enteredUsage: String) {
        validateUsage()
    }
           
   private func validateUsage() {
        validateField($usage, $usageValidationResult, $isValidUsageEntered) { inputValidator.validateUsage(usageToTest: self.usage) }
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
                && isValidRemitteeNameEntered
                && isValidRemitteeIbanEntered
                && isValidRemitteeBicEntered
                && isValidAmountEntered
                && isValidUsageEntered
    }
    
    private func transferMoney() {
        if let amount = inputValidator.convertAmountString(enteredAmountString: self.amount) {
            isTransferringMoney = true
            
            let data = TransferMoneyData(account: account!, creditorName: remitteeName, creditorIban: remitteeIban, creditorBic: remitteeBic, amount: amount, usage: usage, instantPayment: instantPayment)
            
            presenter.transferMoneyAsync(data: data) { response in
                self.handleTransferMoneyResponse(data, response)
            }
        }
    }
    
    private func handleTransferMoneyResponse(_ data: TransferMoneyData, _ response: BankingClientResponse) {
        isTransferringMoney = false
        
        if (response.isSuccessful) {
            self.transferMoneyResponseMessage = Message(message: Text("Successfully transferred \(data.amount) \("€") to \(data.creditorName)."), primaryButton: .ok {
                self.presentation.wrappedValue.dismiss()
            })
        }
        else if response.userCancelledAction == false {
            self.transferMoneyResponseMessage = Message(message: Text("Could not transfer \(data.amount) \("€") to \(data.creditorName). Error: \(response.errorToShowToUser ?? "")."))
        }
    }
}

struct TransferMoneyDialog_Previews: PreviewProvider {
    static var previews: some View {
        TransferMoneyDialog()
    }
}
