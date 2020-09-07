import SwiftUI
import BankingUiSwift
import Combine


extension View {
    
    func fixKeyboardCoversLowerPart() -> some View {
        return self.modifier(AdaptsToKeyboard())
    }
    
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
            .navigationBarItems(leading: createCancelButton(onBackButtonPressed))
    }
    
    func setCancelAndDoneNavigationBarButtons(onCancelPressed: @escaping () -> Void, onDonePressed: @escaping () -> Void) -> some View {
        return self
            .navigationBarHidden(false)
            .navigationBarItems(leading: createCancelButton(onCancelPressed), trailing: createDoneButton(onDonePressed))
    }
    
    func createDoneButton(_ onDoneButtonPressed: @escaping () -> Void) -> some View {
        return Button(action: onDoneButtonPressed) {
            Text("Done")
            .edgesIgnoringSafeArea(.leading)
            .padding(.leading, 0)
        }
    }
    
    func createCancelButton(_ onCancelButtonPressed: @escaping () -> Void) -> some View {
        return Button(action: onCancelButtonPressed) {
            HStack {
                Image(systemName: "chevron.left")
                    .font(.headline)
                    .padding(.horizontal, 0)
                
                Text("Cancel")
                    .padding(.leading, 0)
            }
            .edgesIgnoringSafeArea(.leading)
            .padding(.leading, 0)
        }
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
    
    func systemGroupedBackground() -> some View {
        return self.background(Color.systemGroupedBackground)
    }
    
    func styleAmount(amount: CommonBigDecimal) -> some View {
        let amountColor = amount.decimal.isPositive() ? Styles.PositiveAmountColor : Styles.NegativeAmountColor
        
        return self
            .detailFont()
            .foregroundColor(amountColor)
    }
    
    
    func alignHorizontally(_ alignment: Alignment) -> some View {
        return self.frame(maxWidth: .infinity, alignment: alignment)
    }
    
    
    func turnAnimationOff() -> some View {
        return self.animation(nil)
    }
    
    func executeMutatingMethod(method: @escaping () -> Void) -> some View {
        let timerPublisher = Timer.publish(every: 0.1, on: .main, in: .common)
        
        return self.onReceive(timerPublisher.autoconnect()) { _ in
            timerPublisher.connect().cancel()
            
            method()
        }
    }


    func alert(message: Binding<Message?>) -> some View {
        return self.alert(item: message) { message in
            if let seconaryButton = message.secondaryButton {
                return Alert(title: message.title, message: message.message, primaryButton: message.primaryButton, secondaryButton: seconaryButton)
            }
            else {
                return Alert(title: message.title, message: message.message, dismissButton: message.primaryButton)
            }
        }
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
    
    static let systemGroupedBackground = Color(UIColor.systemGroupedBackground)

    // There are more..
    
    static var destructive: Color {
        if UIColor.responds(to: Selector(("_systemDestructiveTintColor"))) {
            if let red = UIColor.perform(Selector(("_systemDestructiveTintColor")))?.takeUnretainedValue() as? UIColor {
                return Color(red)
            }
        }
        
        return Color.red
    }
}


extension Alert.Button {
    
    static func `default`(_ label: String, _ action: (() -> Void)? = {}) -> Alert.Button {
        return .default(Text(label), action: action)
    }
    
    static func ok(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .default("OK", action)
    }
    
    static func discard(_ action: (() -> Void)? = {}) -> Alert.Button {
        return .destructive(Text("Discard"), action: action)
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


extension NumberFormatter {

    static var currency: NumberFormatter {
        let formatter = NumberFormatter()

        formatter.numberStyle = .currency

        return formatter
    }

    static func decimal(_ countMaxDecimalPlaces: Int) -> NumberFormatter {
        let formatter = NumberFormatter()

        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = countMaxDecimalPlaces

        return formatter
    }

}
