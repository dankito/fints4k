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
    
    @State private var showRemitteeAutocompleteList = false
    @State private var remitteeSearchResults = [Remittee]()
    
    @State private var remitteeIban: String = ""
    @State private var isValidRemitteeIbanEntered = false
    
    @State private var remitteeBic: String = ""
    @State private var isValidRemitteeBicEntered = false
    
    @State private var amount = ""
    @State private var isValidAmountEntered = false
    
    @State private var usage: String = ""
    @State private var isValidUsageEntered = true
    
    @State private var instantPayment = false
    
    @State private var transferMoneyResponseMessage: Message? = nil
    
    
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
                UIKitTextField("Remittee Name", text: $remitteeName, focusOnStart: true, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress) { newValue in
                        self.isValidRemitteeNameEntered = self.remitteeName.isNotBlank
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
                
                UIKitTextField("Remittee IBAN", text: $remitteeIban, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress) { newValue in
                        self.isValidRemitteeIbanEntered = newValue.count > 14 // TODO: implement real check if IBAN is valid
                        self.tryToGetBicFromIban(newValue)
                    }
            }
            
            Section {
                UIKitTextField("Amount", text: $amount, keyboardType: .decimalPad, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress) { newValue in
                        // TODO: implement DecimalTextField / NumericTextField
                        let filtered = newValue.filter { "0123456789,".contains($0) }
                        if filtered != newValue {
                            self.amount = filtered
                        }
                        
                        self.isValidAmountEntered = self.amount.isNotBlank
                    }
                
                UIKitTextField("Usage", text: $usage, actionOnReturnKeyPress: handleReturnKeyPress) { newValue in
                        self.isValidUsageEntered = true
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
    
    
    func tryToGetBicFromIban(_ enteredIban: String) {
        let foundBank = presenter.findUniqueBankForIban(iban: enteredIban)
        
        if let foundBank = foundBank {
            self.remitteeBic = foundBank.bic
        }
        else {
            self.remitteeBic = ""
        }

        // TODO: implement a better check if entered BIC is valid (e.g. if format is ABCDDEXX123)
        self.isValidRemitteeBicEntered = self.remitteeBic.count == 8 || self.remitteeBic.count == 11
    }
    
    
    func isRequiredDataEntered() -> Bool {
        return account != nil
                && isValidRemitteeNameEntered
                && isValidRemitteeIbanEntered
                && isValidRemitteeBicEntered
                && isValidAmountEntered
                && isValidUsageEntered
    }
    
    func transferMoney() {
        let data = TransferMoneyData(creditorName: remitteeName, creditorIban: remitteeIban, creditorBic: remitteeBic, amount: CommonBigDecimal(decimal: amount.replacingOccurrences(of: ",", with: ".")), usage: usage, instantPayment: instantPayment)
        
        presenter.transferMoneyAsync(bankAccount: account!, data: data) { response in
            self.handleTransferMoneyResponse(data, response)
        }
    }
    
    func handleTransferMoneyResponse(_ data: TransferMoneyData, _ response: BankingClientResponse) {
        if (response.isSuccessful) {
            self.transferMoneyResponseMessage = Message(message: Text("Successfully transferred \(data.amount) \("€") to \(data.creditorName)"), primaryButton: .ok {
                self.presentation.wrappedValue.dismiss()
            })
        }
        else {
            self.transferMoneyResponseMessage = Message(message: Text("Could not transfer \(data.amount) \("€") to \(data.creditorName). Error: \(response.errorToShowToUser ?? response.error?.message ?? "")."))
        }
    }
}

struct TransferMoneyDialog_Previews: PreviewProvider {
    static var previews: some View {
        TransferMoneyDialog()
    }
}
