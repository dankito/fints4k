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
    
    @State private var instantPayment = false
    
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
        
        self._usage = State(initialValue: preselectedValues.usage)
        
        if preselectedValues.amount.decimal != NSDecimalNumber.zero {
            self._amount = State(initialValue: preselectedValues.amount.format(countDecimalPlaces: 2))
        }
        
        if preselectedBankAccount.supportsInstantPaymentMoneyTransfer {
            self._instantPayment = State(initialValue: preselectedValues.instantPayment)
        }
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
                                       isFocussedChanged: validateRemitteeNameOnFocusLost, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: validateRemitteeName)

                remitteeNameValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                if self.showRemitteeAutocompleteList {
                    Section {
                        List(self.remitteeSearchResults) { remittee in
                            RemitteeListItem(remittee: remittee)
                                .onTapGesture { self.remitteeSelected(remittee) }
                        }
                        .padding(.vertical, 12)
                    }
                }
                
                LabelledUIKitTextField(label: "Remittee IBAN", text: $remitteeIban, focusNextTextFieldOnReturnKeyPress: true, isFocussedChanged: validateRemitteeIbanOnFocusLost,
                                       actionOnReturnKeyPress: handleReturnKeyPress, textChanged: validateRemitteeIban)

                remitteeIbanValidationResult.map { validationError in
                    ValidationLabel(validationError)
                }
                
                remitteeBankInfo.map {
                    InfoLabel($0)
                    .font(.caption)
                }
            }
            
            Section {
                LabelledUIKitTextField(label: "Amount", text: $amount, keyboardType: .decimalPad, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress, textChanged: validateAmount)

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
                    Button(action: { self.transferMoney() },
                           label: { Text("Transfer Money") })
                        .disabled( !self.isRequiredDataEntered())
                    Spacer()
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
    
    
    func handleReturnKeyPress() -> Bool {
        if self.isRequiredDataEntered() {
            self.transferMoney()
            
            return true
        }
        
        return false
    }

    
    private func validateRemitteeName(enteredRemitteeName: String) {
        validateField($remitteeName, $remitteeNameValidationResult, $isValidRemitteeNameEntered) {
            inputValidator.validateRemitteeNameWhileTyping(remitteeNameToTest: remitteeName)
        }
    }
    
    private func validateRemitteeNameOnFocusLost(_ isFocussed: Bool) {
        if isFocussed == false {
            validateField($remitteeName, $remitteeNameValidationResult, $isValidRemitteeNameEntered) {
                inputValidator.validateRemitteeName(remitteeNameToTest: remitteeName)
            }
        }
    }
    
    
    private func validateRemitteeIban(_ enteredIban: String) {
        validateField($remitteeIban, $remitteeIbanValidationResult, $isValidRemitteeIbanEntered) { inputValidator.validateIbanWhileTyping(ibanToTest: enteredIban) }
         
        tryToGetBicFromIban(enteredIban)
    }
    
    private func validateRemitteeIbanOnFocusLost(_ isFocussed: Bool) {
        if isFocussed == false {
            validateField($remitteeIban, $remitteeIbanValidationResult, $isValidRemitteeIbanEntered) { inputValidator.validateIban(ibanToTest: remitteeIban) }
        }
    }
    
    private func tryToGetBicFromIban(_ enteredIban: String) {
        let foundBank = presenter.findUniqueBankForIban(iban: enteredIban)
        
        if let foundBank = foundBank {
            self.remitteeBic = foundBank.bic
            self.remitteeBankInfo = "BIC: \(foundBank.bic), \(foundBank.name)"
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

        // TODO: implement a better check if entered BIC is valid (e.g. if format is ABCDDEXX123)
        self.isValidRemitteeBicEntered = self.remitteeBic.count == 8 || self.remitteeBic.count == 11
    }
    
    
    private func validateAmount(_ enteredAmount: String) {
        // TODO: implement DecimalTextField / NumericTextField
        let filtered = enteredAmount.filter { "0123456789,".contains($0) }
        if filtered != enteredAmount {
            self.amount = filtered
            
            return // don't validate field after non decimal character has been entered
        }
        
        if amount.isNotBlank {
            validateField($amount, $amountValidationResult, $isValidAmountEntered) { inputValidator.validateAmount(enteredAmountString: enteredAmount) }
        }
        else {
            isValidAmountEntered = false
            amountValidationResult = nil
        }
    }
        
        
    private func validateUsage(enteredUsage: String) {
        validateField($usage, $usageValidationResult, $isValidUsageEntered) { inputValidator.validateUsage(usageToTest: enteredUsage) }
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
            let data = TransferMoneyData(creditorName: remitteeName, creditorIban: remitteeIban, creditorBic: remitteeBic, amount: amount, usage: usage, instantPayment: instantPayment)
            
            presenter.transferMoneyAsync(bankAccount: account!, data: data) { response in
                self.handleTransferMoneyResponse(data, response)
            }
        }
    }
    
    private func handleTransferMoneyResponse(_ data: TransferMoneyData, _ response: BankingClientResponse) {
        if (response.isSuccessful) {
            self.transferMoneyResponseMessage = Message(message: Text("Successfully transferred \(data.amount) \("€") to \(data.creditorName)"), primaryButton: .ok {
                self.presentation.wrappedValue.dismiss()
            })
        }
        else {
            self.transferMoneyResponseMessage = Message(message: Text("Could not transfer \(data.amount) \("€") to \(data.creditorName). Error: \(response.errorToShowToUser ?? "")."))
        }
    }
}

struct TransferMoneyDialog_Previews: PreviewProvider {
    static var previews: some View {
        TransferMoneyDialog()
    }
}
