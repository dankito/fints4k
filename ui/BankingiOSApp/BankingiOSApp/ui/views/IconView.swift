import SwiftUI
import BankingUiSwift


struct IconView: View {
    
    let iconData: KotlinByteArray?
    
    let defaultIconName: String
    
    
    @Inject private var persistence: CoreDataBankingPersistence
    

    var body: some View {
        getBankIcon(self.iconData)
        .renderingMode(Image.TemplateRenderingMode.original)
        .resizable()
        .scaledToFit()
        .frame(width: Styles.AccountsIconWidth)
    }
    
    private func getBankIcon(_ iconData: KotlinByteArray?) -> Image {
        if let iconData = iconData {
            let nsData = ByteArrayExtensions.Companion().toNSData(array: iconData)
            
            if let uiImage = UIImage(data: nsData) {
                return Image(uiImage: uiImage)
            }
        }

        return Image(defaultIconName)
    }

}


struct IconView_Previews: PreviewProvider {

    static var previews: some View {
        IconView(iconData: nil, defaultIconName: "")
    }

}
