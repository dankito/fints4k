import SwiftUI


extension View {
    
    func hideNavigationBar() -> some View {
        return self
            .navigationBarHidden(true)
            .navigationBarTitle("", displayMode: .inline)
    }
    
    func showNavigationBarTitle(_ title: LocalizedStringKey, displayMode: NavigationBarItem.TitleDisplayMode = .inline) -> some View {
        return self
            .navigationBarHidden(false)
            .navigationBarTitle(title, displayMode: displayMode)
    }
    
}


extension Alert.Button {
    
    public static func ok(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .default(Text("OK"), action: action)
    }
    
}
