import SwiftUI


struct TrianglePointingDown: Shape {
    
    func path(in rect: CGRect) -> Path {
        var path = Path()
        
        path.move(to: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY))

        return path
    }

}


struct TrianglePointingDown_Previews: PreviewProvider {

    static var previews: some View {
        TrianglePointingDown()
            .frame(width: 300, height: 300)
    }

}
