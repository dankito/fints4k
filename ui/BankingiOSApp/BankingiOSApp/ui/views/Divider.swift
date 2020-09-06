import SwiftUI


struct Divider: View {
    
    var height: CGFloat = 1
    
    var color: Color = Color.black
    

    var body: some View {
        Rectangle()
        .frame(height: height)
        .background(color)
        .listRowInsets(EdgeInsets())
        .edgesIgnoringSafeArea(.horizontal)
        .padding(0)
    }

}


struct Line_Previews: PreviewProvider {

    static var previews: some View {
        Divider()
    }

}
