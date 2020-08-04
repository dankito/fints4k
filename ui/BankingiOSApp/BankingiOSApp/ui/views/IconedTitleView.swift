import SwiftUI
import BankingUiSwift


struct IconedTitleView: View {
    
    private var title: String
    
    private var iconUrl: String?
    
    private var defaultIconName: String
    
    private var titleFont: Font?
    
    
    init(_ bank: Customer, titleFont: Font? = nil) {
        self.init(accountTitle: bank.displayName, iconUrl: bank.iconUrl, defaultIconName: Styles.AccountFallbackIcon, titleFont: titleFont)
    }
    
    init(_ account: BankAccount, titleFont: Font? = nil) {
        self.init(accountTitle: account.displayName, iconUrl: account.customer.iconUrl, defaultIconName: Styles.AccountFallbackIcon, titleFont: titleFont)
    }
    
    init(accountTitle: String, iconUrl: String?, defaultIconName: String, titleFont: Font? = nil) {
        self.title = accountTitle
        self.iconUrl = iconUrl
        
        self.defaultIconName = defaultIconName
        self.titleFont = titleFont
    }
    

    var body: some View {
        HStack {
            IconView(iconUrl: self.iconUrl, defaultIconName: self.defaultIconName)
            
            getTitleView()
        }
    }

    
    private func getTitleView() -> Text {
        if let titleFont = titleFont {
            return Text(title)
                .font(titleFont)
        }
        else {
            return Text(title)
        }
    }

}


struct IconedAccountTitle_Previews: PreviewProvider {

    static var previews: some View {
        IconedTitleView(accountTitle: "Abzockbank", iconUrl: nil, defaultIconName: Styles.AccountFallbackIcon)
    }

}
