import SwiftUI


struct LabelledUIKitTextField: View {
    
    let label: LocalizedStringKey
    
    @Binding var text: String
    
    var placeholder: String = ""
    
    var keyboardType: UIKeyboardType = .default
    var isPasswordField: Bool = false
    
    var focusOnStart = false
    var focusNextTextFieldOnReturnKeyPress = false
    
    var isUserInputEnabled: Bool = true
    
    var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    var textChanged: ((String) -> Void)? = nil
    

    var body: some View {
        HStack {
            Text(label)
            
            Spacer()
            
            UIKitTextField(placeholder, text: $text, keyboardType: keyboardType, isPasswordField: isPasswordField,
                           focusOnStart: focusOnStart, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress,
                           textAlignment: .right, isUserInputEnabled: isUserInputEnabled,
                           actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
        }
    }

}


struct LabelledUIKitTextField_Previews: PreviewProvider {

    static var previews: some View {
        LabelledUIKitTextField(label: "Label", text: .constant("Text"))
    }

}


extension LabelledUIKitTextField {
    
    init(label: LocalizedStringKey, value: String) {
        self.label = label
        
        _text = .constant("")
        
        self.placeholder = value
        
        self.isUserInputEnabled = false
    }
    
}