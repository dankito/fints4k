import SwiftUI
import BankingUiSwift


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
    
    func styleAmount(amount: CommonBigDecimal) -> some View {
        let amountColor = amount.decimal.isPositive() ? Styles.PositiveAmountColor : Styles.NegativeAmountColor
        
        return self
            .detailFont()
            .foregroundColor(amountColor)
    }
    
}


extension Color {
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


extension Binding {
    
    func willSet(_ execute: @escaping (_ currentValue: Value, _ nextValue: Value) ->Void) -> Binding {
        return Binding(
            get: {
                return self.wrappedValue
            },
            set: {
                execute(self.wrappedValue, $0)
                
                self.wrappedValue = $0
            }
        )
    }
    
    func didSet(_ execute: @escaping (_ oldValue: Value, _ newValue: Value) ->Void) -> Binding {
        return Binding(
            get: {
                return self.wrappedValue
            },
            set: {
                let oldValue = self.wrappedValue
                
                self.wrappedValue = $0
                
                execute(oldValue, $0)
            }
        )
    }
    
}
