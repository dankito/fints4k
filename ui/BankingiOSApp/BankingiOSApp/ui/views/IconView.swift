import SwiftUI


struct IconView: View {
    
    let iconUrl: String?
    
    let defaultIconName: String
    
    
    @Inject private var persistence: CoreDataBankingPersistence
    

    var body: some View {
        getBankIcon(self.iconUrl)
        .renderingMode(Image.TemplateRenderingMode.original)
        .resizable()
        .scaledToFit()
        .frame(width: Styles.AccountsIconWidth)
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

}


struct IconView_Previews: PreviewProvider {

    static var previews: some View {
        IconView(iconUrl: nil, defaultIconName: "")
    }

}
