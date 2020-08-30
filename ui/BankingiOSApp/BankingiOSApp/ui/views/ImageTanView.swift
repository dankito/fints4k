import SwiftUI
import BankingUiSwift


struct ImageTanView: View {
    
    private static let ImageTanWidthDefaultsKey = "ImageTanWidth"
    
    
    private var tanChallenge: ImageTanChallenge
    
    private var imageData: Data
    
    @State private var imageWidth = CGFloat(UserDefaults.standard.float(forKey: Self.ImageTanWidthDefaultsKey, defaultValue: Float(UIScreen.main.bounds.width / 2)))
    
    
    init(_ tanChallenge: ImageTanChallenge) {
        self.tanChallenge = tanChallenge
        
        self.imageData = tanChallenge.image.imageBytesAsNSData()
    }
    
    
    var body: some View {
        Section {
            ScaleImageView($imageWidth.didSet(self.imageWidthDidChange))
            
            HStack {
                Spacer()
                
                UIKitImageView(data: self.imageData)
                    .frame(width: imageWidth, height: imageWidth)
                    .padding(.horizontal, -32.0)
                
                Spacer()
            }
        }
    }
    
    
    private func imageWidthDidChange(oldValue: CGFloat?, newValue: CGFloat?) {
        if let newValue = newValue {
            UserDefaults.standard.set(newValue, forKey: Self.ImageTanWidthDefaultsKey)
        }
    }
    
}


struct ImageTanView_Previews: PreviewProvider {
    
    static var previews: some View {
        return ImageTanView(previewImageTanChallenge)
    }
    
}
