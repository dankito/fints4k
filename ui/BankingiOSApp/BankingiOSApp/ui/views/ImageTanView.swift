import SwiftUI
import BankingUiSwift


struct ImageTanView: View {
    
    private var tanChallenge: ImageTanChallenge
    
    private var imageData: Data
    
    @State private var imageWidth: CGFloat = UIScreen.main.bounds.width / 2
    
    
    init(_ tanChallenge: ImageTanChallenge) {
        self.tanChallenge = tanChallenge
        
        self.imageData = tanChallenge.image.imageBytesAsNSData()
    }
    
    
    var body: some View {
        Section {
            ScaleImageView($imageWidth)
            
            HStack {
                Spacer()
                
                UIKitImageView(data: self.imageData)
                    .frame(width: imageWidth, height: imageWidth)
                    .padding(.horizontal, -32.0)
                
                Spacer()
            }
        }
    }
    
}


struct ImageTanView_Previews: PreviewProvider {
    
    static var previews: some View {
        return ImageTanView(previewImageTanChallenge)
    }
    
}
