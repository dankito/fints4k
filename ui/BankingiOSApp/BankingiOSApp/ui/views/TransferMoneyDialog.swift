import SwiftUI
import BankingUiSwift
import Combine


struct TransferMoneyDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private var showAccounts = false
    
    private var accountsSupportingTransferringMoney: [BUCBankAccount] = []
    
    @State private var selectedAccountIndex = 0
    private var account: BUCBankAccount? = nil
    
    @State private var remitteeName: String = ""
    @State private var isValidRemitteeNameEntered = false
    
    @State private var remitteeIban: String = ""
    @State private var isValidRemitteeIbanEntered = false
    
    @State private var remitteeBic: String = "" // TODO
    @State private var isValidRemitteeBicEntered = true // TODO
    
    @State private var amount = ""
    @State private var isValidAmountEntered = false
    
    @State private var usage: String = ""
    @State private var isValidUsageEntered = true
    
    @State private var instantPayment = false
    
    
    private var supportsInstantPayment: Bool {
        return self.account?.supportsInstantPaymentMoneyTransfer ?? false
    }
    
    
    @State private var transferMoneyResponseMessage: Message? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init() {
        self.accountsSupportingTransferringMoney = self.presenter.bankAccounts.filter({ $0.supportsTransferringMoney })
        
        self.account = self.accountsSupportingTransferringMoney.first
        
        self.showAccounts = self.accountsSupportingTransferringMoney.count > 1
    }
    
    
    var body: some View {
        Form {
            if (showAccounts) {
                Section {
                    Picker("Account", selection: $selectedAccountIndex) {
                        ForEach(0 ..< self.accountsSupportingTransferringMoney.count) { accountIndex in
                            Text(self.accountsSupportingTransferringMoney[accountIndex].displayName)
                        }
                    }
                }
            }
            
            Section {
                TextField("Remittee Name", text: $remitteeName)
                    .onReceive(Just(remitteeName)) { newValue in
                        self.isValidRemitteeNameEntered = self.remitteeName.isEmpty == false
                    }
                
                TextField("Remittee IBAN", text: $remitteeIban)
                    .onReceive(Just(remitteeIban)) { newValue in
                        self.isValidRemitteeIbanEntered = self.remitteeIban.isEmpty == false
                    }
                
                TextField("Amount", text: $amount)
                    .keyboardType(.decimalPad)
                    .onReceive(Just(amount)) { newValue in
                        let filtered = newValue.filter { "0123456789,".contains($0) }
                        if filtered != newValue {
                            self.amount = filtered
                        }
                        
                        self.isValidAmountEntered = self.amount.isEmpty == false
                    }
                
                TextField("Usage", text: $usage)
                    .onReceive(Just($usage)) { newValue in
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
                        .disabled(!self.isRequiredDataEntered())
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
        .navigationBarTitle("Transfer Money Dialog Title", displayMode: .inline)
    }
    
    
    func isRequiredDataEntered() -> Bool {
        return account != nil
                && isValidRemitteeNameEntered
                && isValidRemitteeIbanEntered
                //&& isValidRemitteeBicEntered
                && isValidAmountEntered
                && isValidUsageEntered
    }
    
    func transferMoney() {
        let data = BUCTransferMoneyData(creditorName: remitteeName, creditorIban: remitteeIban, creditorBic: remitteeBic, amount: CommonBigDecimal(decimal: amount.replacingOccurrences(of: ",", with: ".")), usage: usage, instantPayment: instantPayment)
        
        presenter.transferMoneyAsync(bankAccount: account!, data: data) { response in
            self.handleTransferMoneyResponse(data, response)
        }
    }
    
    func handleTransferMoneyResponse(_ data: BUCTransferMoneyData, _ response: BUCBankingClientResponse) {
        if (response.isSuccessful) {
            self.transferMoneyResponseMessage = Message(message: Text("Successfully transferred \(data.amount) \("€") to \(data.creditorName)"))
            presentation.wrappedValue.dismiss()
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
