import SwiftUI


extension View {
    
    func hideNavigationBar() -> some View {
        return self
            .navigationBarHidden(true)
            .navigationBarTitle("Title")
    }
    
}


extension Alert.Button {
    
    public static func ok(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .default(Text("OK"), action: action)
    }
    
}
