import SwiftUI
import BankingUiSwift


struct IconedTitleView: View {
    
    private var title: String
    
    private var iconUrl: String?
    
    private var defaultIconName: String
    
    private var titleFont: Font?
    
    
    @Inject private var persistence: CoreDataBankingPersistence
    
    
    init(_ bank: Customer, titleFont: Font? = nil) {
        self.init(accountTitle: bank.displayName, iconUrl: bank.iconUrl, defaultIconName: "accounts", titleFont: titleFont)
    }
    
    init(_ account: BankAccount, titleFont: Font? = nil) {
        self.init(accountTitle: account.displayName, iconUrl: account.customer.iconUrl, defaultIconName: "accounts", titleFont: titleFont)
    }
    
    init(accountTitle: String, iconUrl: String?, defaultIconName: String, titleFont: Font? = nil) {
        self.title = accountTitle
        self.iconUrl = iconUrl
        
        self.defaultIconName = defaultIconName
        self.titleFont = titleFont
    }
    

    var body: some View {
        HStack {
            getBankIcon(self.iconUrl)
            .renderingMode(Image.TemplateRenderingMode.original)
            .resizable()
            .scaledToFit()
            .frame(width: 24)
            
            getTitleView()
        }
    }
    
    
    private func getBankIcon(_ iconUrl: String?) -> Image {
        if let iconUrl = iconUrl {
            if let iconData = persistence.readContentOfFile(iconUrl) {
                if let uiImage = UIImage(data: iconData) {
                    return Image(uiImage: uiImage)
                }
            }
        }
        
        return Image(defaultIconName)
    }
    
    private func getTitleView() -> Text {
        if let titleFont = titleFont {
            return Text(self.title)
                .font(titleFont)
        }
        else {
            return Text(title)
        }
    }

}


struct IconedAccountTitle_Previews: PreviewProvider {

    static var previews: some View {
        IconedTitleView(accountTitle: "Abzockbank", iconUrl: nil, defaultIconName: "accounts")
    }

}
