import SwiftUI


struct FaceIDButton: View {
    
    private let widthAndHeight: CGFloat
    
    private let action: () -> Void
    
    
    init(_ action: @escaping () -> Void) {
        self.init(34, action)
    }
    
    init(_ widthAndHeight: CGFloat, _ action: @escaping () -> Void) {
        self.widthAndHeight = widthAndHeight
        self.action = action
    }
    

    var body: some View {
        Button(action: self.action) {
            Image(systemName: "faceid")
                .resizable()
                .frame(width: widthAndHeight, height: widthAndHeight)
        }
    }

}


struct FaceIDButton_Previews: PreviewProvider {

    static var previews: some View {
        FaceIDButton { }
    }

}
