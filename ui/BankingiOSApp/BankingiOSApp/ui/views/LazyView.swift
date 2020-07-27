import SwiftUI


struct LazyView<Content: View>: View {
    
    private let build: () -> Content
    
    init(_ build: @autoclosure @escaping () -> Content) {
        self.build = build
    }
    
    var body: Content {
        build()
    }
}


struct LazyView_Previews: PreviewProvider {
    
    static var previews: some View {
        LazyView(Text("Hello"))
    }
    
}
