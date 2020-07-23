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
    
    func customNavigationBarBackButton(onBackButtonPressed: @escaping () -> Void) -> some View {
        return self
            .navigationBarBackButtonHidden(true)
            .navigationBarItems(leading: Button(action: onBackButtonPressed) {
                HStack {
                    Image(systemName: "chevron.left")
                        .font(.headline)
                        .padding(.horizontal, 0)
                    
                    Text("Cancel")
                        .padding(.leading, 0)
                }
                .edgesIgnoringSafeArea(.leading)
                .padding(.leading, 0)
            })
    }
    
}


extension Alert.Button {
    
    public static func ok(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .default(Text("OK"), action: action)
    }
    
}
