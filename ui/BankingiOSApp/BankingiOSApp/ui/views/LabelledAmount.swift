import SwiftUI
import BankingUiSwift


struct LabelledAmount: View {
    
    private let label: LocalizedStringKey
    
    private let amount: CommonBigDecimal
    
    private let currencyIsoCode: String?
    
    
    init(_ label: LocalizedStringKey, _ amount: CommonBigDecimal, _ currencyIsoCode: String? = nil) {
        self.label = label
        self.amount = amount
        self.currencyIsoCode = currencyIsoCode
    }
    
    init(_ label: String, _ amount: CommonBigDecimal, _ currencyIsoCode: String? = nil) {
        self.init(LocalizedStringKey(label), amount, currencyIsoCode)
    }
    

    var body: some View {
        LabelledObject(label) {
            AmountLabel(amount, currencyIsoCode)
        }
    }

}


struct LabelledDate_Previews: PreviewProvider {

    static var previews: some View {
        LabelledAmount("Amount", CommonBigDecimal(double: 84.25), "EUR")
    }

}
