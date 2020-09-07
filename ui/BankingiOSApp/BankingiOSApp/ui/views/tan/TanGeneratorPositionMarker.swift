import SwiftUI


struct TanGeneratorPositionMarker: View {
    
    static let Width = CGFloat(24)
    
    static let Height = CGFloat(24)
    

    var body: some View {
        TrianglePointingDown()
        .fill(Color.gray)
        .frame(width: Self.Width, height: Self.Height)
    }

}


struct TanGeneratorPositionMarker_Previews: PreviewProvider {

    static var previews: some View {
        TanGeneratorPositionMarker()
    }

}
