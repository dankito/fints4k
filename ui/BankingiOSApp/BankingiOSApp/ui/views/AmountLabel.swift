import SwiftUI
import BankingUiSwift


struct AmountLabel: View {
    
    let amount: CommonBigDecimal
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    var body: some View {
        Text(presenter.formatAmount(amount: amount))
            .styleAmount(amount: amount)
    }
}


struct AmountLabel_Previews: PreviewProvider {
    static var previews: some View {
        AmountLabel(amount: CommonBigDecimal(double: 84.12))
    }
}
