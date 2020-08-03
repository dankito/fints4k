import SwiftUI
import BankingUiSwift


struct ImageTanView: View {
    
    private var tanChallenge: ImageTanChallenge
    
    private var imageData: Data
    
    @State private var imageWidth: CGFloat
    
    private let imageMinWidth: CGFloat
    
    private let imageMaxWidth: CGFloat
    
    private let step: CGFloat
    
    
    init(_ tanChallenge: ImageTanChallenge) {
        self.tanChallenge = tanChallenge
        
        self.imageData = tanChallenge.image.imageBytesAsNSData()
        
        let screenWidth = UIScreen.main.bounds.width
        let screenWidthQuarter = screenWidth / 4
        
        self.imageMinWidth = screenWidthQuarter < 150 ? 150 : screenWidthQuarter // don't know whey but iOS seems that it doesn't scale image smaller than 150
        self.imageMaxWidth = screenWidth
        
        let range = imageMaxWidth - imageMinWidth
        
        self._imageWidth = State(initialValue: imageMinWidth + range / 2)
        
        self.step = range / 20
    }
    
    
    var body: some View {
        Section {
            HStack {
                Text("Size")
                
                Spacer()
                
                Rectangle()
                    .fill(Color.gray)
                    .frame(width: 6, height: 9)
                
                Slider(value: $imageWidth, in: imageMinWidth...imageMaxWidth, step: step)
                
                Rectangle()
                    .fill(Color.gray)
                    .frame(width: 16, height: 19)
            }
            
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
        let tanChallenge = ImageTanChallenge(image: TanImage(mimeType: "image/png", imageBytes: KotlinByteArray(size: 0), decodingError: nil), messageToShowToUser: "", tanProcedure: TanProcedure(displayName: "", type: .phototan, bankInternalProcedureCode: ""))
        
        return ImageTanView(tanChallenge)
    }
    
}
