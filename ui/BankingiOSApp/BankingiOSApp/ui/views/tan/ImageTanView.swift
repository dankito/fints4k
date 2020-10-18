import SwiftUI
import BankingUiSwift


struct ImageTanView: View {
    
    private var tanChallenge: ImageTanChallenge
    
    private var imageData: Data
    
    @State private var imageWidth: CGFloat = CGFloat(UIScreen.main.bounds.width / 2)
    
    private var tanMethodSettings: TanMethodSettings? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
    
    init(_ tanChallenge: ImageTanChallenge) {
        self.tanChallenge = tanChallenge
        
        self.imageData = tanChallenge.image.imageBytesAsNSData()
        
        self.tanMethodSettings = presenter.isQrTanMethod(tanMethod: tanChallenge.tanMethod) ? presenter.appSettings.qrCodeSettings : presenter.appSettings.photoTanSettings
        
        if let imageWidth = tanMethodSettings?.width {
            self._imageWidth = State(initialValue: CGFloat(imageWidth))
        }
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
        .onDisappear {
            self.saveChanges()
        }
    }
    
    
    private func saveChanges() {
        let imageWidthInt = Int32(imageWidth)
        
        if imageWidthInt != tanMethodSettings?.width {
            let settings = tanMethodSettings ?? TanMethodSettings(width: imageWidthInt, height: 0, space: 0, frequency: 0)
            settings.width = imageWidthInt
            
            presenter.updateTanMethodSettings(tanMethod: tanChallenge.tanMethod, settings: settings)
        }
    }
    
}


struct ImageTanView_Previews: PreviewProvider {
    
    static var previews: some View {
        return ImageTanView(previewImageTanChallenge)
    }
    
}
