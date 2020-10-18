import SwiftUI


struct ScaleImageView: View {
    
    @Binding private var imageWidth: CGFloat
    
    private let imageMinWidth: CGFloat
    
    private let imageMaxWidth: CGFloat
    
    private let step: CGFloat
    
    
    init(_ imageWidth: Binding<CGFloat>) {
        let screenWidth = UIScreen.main.bounds.width
        let screenWidthQuarter = screenWidth / 4
        
        let imageMinWidth = screenWidthQuarter < 150 ? 150 : screenWidthQuarter // don't know whey but iOS seems that it doesn't scale image smaller than 150
        let imageMaxWidth = screenWidth
        
        let range = imageMaxWidth - imageMinWidth
        
        self.init(imageWidth, imageMinWidth: imageMinWidth, imageMaxWidth: imageMaxWidth, step: range / 20)
    }
    
    init(_ imageWidth: Binding<CGFloat>, imageMinWidth: CGFloat, imageMaxWidth: CGFloat, step: CGFloat) {
        _imageWidth = imageWidth
        
        self.imageMinWidth = imageMinWidth
        self.imageMaxWidth = imageMaxWidth
        
        self.step = step
    }
    
    
    var body: some View {
        HStack {
            Text("Size")
            
            Spacer()
            
            Rectangle()
                .fill(Color.gray) // TODO: use a system color
                .frame(width: 6, height: 9)
            
            Slider(value: $imageWidth, in: imageMinWidth...imageMaxWidth, step: step)
            
            Rectangle()
                .fill(Color.gray) // TODO: use a system color
                .frame(width: 16, height: 19)
        }
    }

}


struct ScaleImageView_Previews: PreviewProvider {

    static var previews: some View {
        ScaleImageView(.constant(250))
    }

}
