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
    
    func detailForegroundColor() -> some View {
        return self
            .foregroundColor(Color.secondary)
    }
    
    func detailFont() -> some View {
        return self
        .font(.callout)
    }
    
    func styleAsDetail() -> some View {
        return self
            .detailFont()
            .detailForegroundColor()
    }
    
}


public extension Color {
    static let lightText = Color(UIColor.lightText)
    static let darkText = Color(UIColor.darkText)

    static let label = Color(UIColor.label)
    static let secondaryLabel = Color(UIColor.secondaryLabel)
    static let tertiaryLabel = Color(UIColor.tertiaryLabel)
    static let quaternaryLabel = Color(UIColor.quaternaryLabel)

    static let systemBackground = Color(UIColor.systemBackground)
    static let secondarySystemBackground = Color(UIColor.secondarySystemBackground)
    static let tertiarySystemBackground = Color(UIColor.tertiarySystemBackground)

    // There are more..
}


extension Alert.Button {
    
    public static func ok(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .default(Text("OK"), action: action)
    }
    
}
