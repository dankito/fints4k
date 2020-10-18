import SwiftUI
import BankingUiSwift


struct AmountLabel: View {
    
    private let amount: CommonBigDecimal
    
    private let currencyIsoCode: String?
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ amount: CommonBigDecimal, _ currencyIsoCode: String? = nil) {
        self.amount = amount
        self.currencyIsoCode = currencyIsoCode
    }
    
    
    var body: some View {
        Text(presenter.formatAmount(amount: amount, currencyIsoCode: currencyIsoCode))
            .styleAmount(amount: amount)
    }
}


struct AmountLabel_Previews: PreviewProvider {
    static var previews: some View {
        AmountLabel(CommonBigDecimal(double: 84.12))
    }
}
