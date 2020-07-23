import SwiftUI
import BankingUiSwift


struct ImageTanView: View {
    
    private var tanChallenge: ImageTanChallenge
    
    private var imageData: Data
    
    
    init(_ tanChallenge: ImageTanChallenge) {
        self.tanChallenge = tanChallenge
        
        self.imageData = tanChallenge.image.imageBytesAsNSData()
    }
    
    
    var body: some View {
        UIKitImageView(data: self.imageData)
    }
}


struct ImageTanView_Previews: PreviewProvider {
    
    static var previews: some View {
        let tanChallenge = ImageTanChallenge(image: TanImage(mimeType: "image/png", imageBytes: KotlinByteArray(size: 0), decodingError: nil), messageToShowToUser: "", tanProcedure: TanProcedure(displayName: "", type: .phototan, bankInternalProcedureCode: ""))
        
        return ImageTanView(tanChallenge)
    }
    
}
