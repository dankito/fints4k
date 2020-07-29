import SwiftUI


struct UIKitTextField: UIViewRepresentable {
    
    static private var NextTagId = 234567 // start at a high, very unlikely number to not interfere with manually set tags

    
    @Binding private var text: String
    
    private var placeHolder: String
    
    private var keyboardType: UIKeyboardType = .default
    private var isPasswordField: Bool = false
    
    private var focusNextTextFieldOnReturnKeyPress = false
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    
    init(_ titleKey: String, text: Binding<String>, keyboardType: UIKeyboardType = .default, isPasswordField: Bool = false,
         focusNextTextFieldOnReturnKeyPress: Bool = false, actionOnReturnKeyPress: (() -> Bool)? = nil) {
        self.placeHolder = titleKey
        _text = text
        
        self.keyboardType = keyboardType
        self.isPasswordField = isPasswordField
        
        self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
    }
    

    func makeUIView(context: UIViewRepresentableContext<UIKitTextField>) -> UITextField {
        let textField = UITextField(frame: .zero)
        
        textField.placeholder = placeHolder.localize()
        
        textField.keyboardType = keyboardType
        textField.isSecureTextEntry = isPasswordField
        
        textField.delegate = context.coordinator
        
        Self.NextTagId = Self.NextTagId + 1 // unbelievable, there's no ++ operator
        textField.tag = Self.NextTagId
        
        return textField
    }

    func makeCoordinator() -> UIKitTextField.Coordinator {
        return Coordinator(text: $text, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress, actionOnReturnKeyPress: actionOnReturnKeyPress)
    }

    func updateUIView(_ uiView: UITextField, context: UIViewRepresentableContext<UIKitTextField>) {
         uiView.text = text
    }


     class Coordinator: NSObject, UITextFieldDelegate {

        @Binding private var text: String
        
        private var focusNextTextFieldOnReturnKeyPress: Bool
        
        private var actionOnReturnKeyPress: (() -> Bool)?


        init(text: Binding<String>, focusNextTextFieldOnReturnKeyPress: Bool, actionOnReturnKeyPress: (() -> Bool)? = nil) {
            _text = text
            
            self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
            
            self.actionOnReturnKeyPress = actionOnReturnKeyPress
        }

        func textFieldDidChangeSelection(_ textField: UITextField) {
            text = textField.text ?? ""
        }
        
        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            var didHandleReturnKey = actionOnReturnKeyPress?() ?? false
            
            if didHandleReturnKey == false && focusNextTextFieldOnReturnKeyPress == true {
                let nextViewTag = textField.tag + 1
                
                let nextView = textField.superview?.superview?.superview?.viewWithTag(nextViewTag)
                    ?? textField.superview?.superview?.superview?.superview?.superview?.viewWithTag(nextViewTag) // for text fields in Lists (tables)
                
                didHandleReturnKey = nextView?.becomeFirstResponder() ?? false
            }
            
            return didHandleReturnKey
        }

    }
    
}


struct UIKitTextView_Previews: PreviewProvider {

    @State static private var text = ""
    
    static var previews: some View {
        UIKitTextField("Test label", text: $text)
    }

}
