import SwiftUI


struct UIKitTextField: UIViewRepresentable {
    
    static private var NextTagId = 234567 // start at a high, very unlikely number to not interfere with manually set tags

    
    @Binding private var text: String
    
    private var placeHolder: String
    
    private var keyboardType: UIKeyboardType = .default
    private var isPasswordField: Bool = false
    
    private var focusOnStart = false
    private var focusNextTextFieldOnReturnKeyPress = false
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    private var textChanged: ((String) -> Void)? = nil
    
    @State private var textField: UITextField? = nil
    
    
    init(_ titleKey: String, text: Binding<String>, keyboardType: UIKeyboardType = .default, isPasswordField: Bool = false,
         focusOnStart: Bool = false, focusNextTextFieldOnReturnKeyPress: Bool = false, actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
        self.placeHolder = titleKey
        _text = text
        
        self.keyboardType = keyboardType
        self.isPasswordField = isPasswordField
        
        self.focusOnStart = focusOnStart
        self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
        self.textChanged = textChanged
    }
    

    func makeUIView(context: UIViewRepresentableContext<UIKitTextField>) -> UITextField {
        let textField = UITextField(frame: .zero)
        
        textField.placeholder = placeHolder.localize()
        
        textField.keyboardType = keyboardType
        textField.isSecureTextEntry = isPasswordField
        
        textField.delegate = context.coordinator
        
        // set tag on all TextFields to be able to focus next view (= next tag). See Coordinator for more details
        Self.NextTagId = Self.NextTagId + 1 // unbelievable, there's no ++ operator
        textField.tag = Self.NextTagId
        
        DispatchQueue.main.async { // to not update state on view updates (and i only need @State as structs cannot be modified)
            self.textField = textField
        }
        
        if focusOnStart {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { // give view some time to show
                textField.becomeFirstResponder()
            }
        }
        
        return textField
    }

    func updateUIView(_ uiView: UITextField, context: UIViewRepresentableContext<UIKitTextField>) {
         uiView.text = text
    }
    

    func makeCoordinator() -> UIKitTextField.Coordinator {
        return Coordinator(text: $text, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress, actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
    }
    
    
    func focus() -> Bool {
        return textField?.becomeFirstResponder() ?? false
    }
    
    func clearFocus() -> Bool {
        return textField?.resignFirstResponder() ?? false
    }


     class Coordinator: NSObject, UITextFieldDelegate {

        @Binding private var text: String
        
        private var focusNextTextFieldOnReturnKeyPress: Bool
        
        private var actionOnReturnKeyPress: (() -> Bool)?
        
        private var textChanged: ((String) -> Void)?


        init(text: Binding<String>, focusNextTextFieldOnReturnKeyPress: Bool, actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
            _text = text
            
            self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
            
            self.actionOnReturnKeyPress = actionOnReturnKeyPress
            
            self.textChanged = textChanged
        }

        func textFieldDidChangeSelection(_ textField: UITextField) {
            let newText = textField.text ?? ""
            let didTextChange = newText != text // e.g. if just the cursor has been placed to another position then textFieldDidChangeSelection() gets called but text didn't change

            DispatchQueue.main.async { // to not update state during view update
                self.text = newText

                if didTextChange {
                    self.textChanged?(newText)
                }
            }
        }
        
        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            var didHandleReturnKey = actionOnReturnKeyPress?() ?? false
            
            if didHandleReturnKey == false && focusNextTextFieldOnReturnKeyPress == true {
                let nextViewTag = textField.tag + 1
                
                let nextView = textField.superview?.superview?.superview?.viewWithTag(nextViewTag)
                    ?? textField.superview?.superview?.superview?.superview?.superview?.viewWithTag(nextViewTag) // for text fields in Lists (tables)
                
                didHandleReturnKey = nextView?.becomeFirstResponder() ?? false
            }

            if didHandleReturnKey == false {
                textField.resignFirstResponder() // default behaviour
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
