import SwiftUI


struct FlickerCodeStripe: View {
    
    private static let Height = CGFloat(100)
    
    
    @Binding private var showBit: Bool
    
    @Binding private var width: CGFloat
    
    
    init(_ showBit: Binding<Bool>, _ width: Binding<CGFloat>) {
        _showBit = showBit
        _width = width
    }
    

    var body: some View {
        Rectangle()
        .fill(showBit ? Color.white : Color.black)
        .frame(width: width, height: Self.Height)
        .turnAnimationOff() // it's very important to turn animation off otherwise stripes get displayed in gray instead of white which TAN generator doesn't recognize
    }

}


struct FlickerCodeBit_Previews: PreviewProvider {

    static var previews: some View {
        let bitWidth: CGFloat = 40
        let spaceWidth: CGFloat = 15
        
        return HStack {
            Spacer()
            
            HStack {
                FlickerCodeStripe(.constant(true), .constant(bitWidth))
                
                Spacer()
                .frame(width: spaceWidth)
                
                FlickerCodeStripe(.constant(true), .constant(bitWidth))
                
                Spacer()
                .frame(width: spaceWidth)
                
                FlickerCodeStripe(.constant(true), .constant(bitWidth))
                
                Spacer()
                .frame(width: spaceWidth)
                
                FlickerCodeStripe(.constant(false), .constant(bitWidth))
                
                Spacer()
                .frame(width: spaceWidth)
                
                FlickerCodeStripe(.constant(true), .constant(bitWidth))
            }
            
            Spacer()
        }
        .frame(width: UIScreen.main.bounds.width)
        .background(Color.black)
    }

}
