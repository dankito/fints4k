import SwiftUI
import BankingUiSwift


struct BankInfoListItem: View {
    
    private let bank: BankInfo
    
    private let onBankSelected: (() -> Void)?
    
    
    init(_ bank: BankInfo, _ onBankSelected: @escaping () -> Void) {
        self.bank = bank
        self.onBankSelected = onBankSelected
    }
    
    init(_ bank: BankInfo) {
        self.bank = bank
        self.onBankSelected = nil
    }
    
    
    var body: some View {
        ZStack {
            VStack {
                HStack {
                    Text(bank.name)
                        .font(.headline)

                    Spacer()
                }

                HStack {
                    Text(bank.bankCode)
                        .bold()
                        .foregroundColor(Color.primary)

                    Text(bank.postalCode)
                        .padding(.leading, 4)

                    Text(bank.city)
                        .lineLimit(1)

                    Spacer()
                }
                .styleAsDetail()
                .padding(.top, 6.0)
            }
            
            // SwiftUI doesn't stretch item to whole width so tap on places without text are not detected -> add a button and pass to caller when tapped on item
            Button("") { self.onBankSelected?() }
        }
        .opacity(bank.supportsFinTs3_0 ? 1.0 : 0.25)
        .accentColor(bank.supportsFinTs3_0 ? .label : .secondaryLabel)
    }
}


struct BankInfoListItem_Previews: PreviewProvider {
    
    static var previews: some View {
        BankInfoListItem(BankInfo(name: "Abzockbank Berlin", bankCode: "12345678", bic: "ABZODEBBXXX", postalCode: "12345", city: "Berlin", pinTanAddress: nil, pinTanVersion: "FinTS 3.0"))
    }
    
}
