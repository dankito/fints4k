import SwiftUI
import BankingUiSwift


struct IconedTitleView: View {
    
    private var title: String
    
    private var iconData: KotlinByteArray?
    
    private var defaultIconName: String
    
    private var titleFont: Font?
    
    
    init(_ bank: IBankData, titleFont: Font? = nil) {
        self.init(accountTitle: bank.displayName, iconData: bank.iconData, defaultIconName: Styles.AccountFallbackIcon, titleFont: titleFont)
    }
    
    init(_ account: IBankAccount, titleFont: Font? = nil) {
        self.init(accountTitle: account.displayName, iconData: account.bank.iconData, defaultIconName: Styles.AccountFallbackIcon, titleFont: titleFont)
    }
    
    init(accountTitle: String, iconData: KotlinByteArray?, defaultIconName: String, titleFont: Font? = nil) {
        self.title = accountTitle
        self.iconData = iconData
        
        self.defaultIconName = defaultIconName
        self.titleFont = titleFont
    }
    

    var body: some View {
        HStack {
            IconView(iconData: self.iconData, defaultIconName: self.defaultIconName)
            
            Spacer()
            .frame(width: Styles.DefaultSpaceBetweenIconAndText)
            
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
        IconedTitleView(accountTitle: "Abzockbank", iconData: nil, defaultIconName: Styles.AccountFallbackIcon)
    }

}
