import SwiftUI


struct UIKitTextField: UIViewRepresentable {
    
    static private var NextTagId = 234567 // start at a high, very unlikely number to not interfere with manually set tags

    
    @Binding private var text: String
    
    private var placeholder: String
    
    private var keyboardType: UIKeyboardType = .default
    private var isPasswordField: Bool = false
    
    private var focusOnStart = false
    private var focusNextTextFieldOnReturnKeyPress = false
    @Binding private var focusTextField: Bool
    
    private var textAlignment: NSTextAlignment = .natural
    private var isUserInputEnabled: Bool = true
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    private var textChanged: ((String) -> Void)? = nil

    
    init(_ titleKey: String, text: Binding<String>, keyboardType: UIKeyboardType = .default, isPasswordField: Bool = false,
         focusOnStart: Bool = false, focusNextTextFieldOnReturnKeyPress: Bool = false, focusTextField: Binding<Bool> = .constant(false),
         textAlignment: NSTextAlignment = .natural, isUserInputEnabled: Bool = true,
         actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
        self.placeholder = titleKey
        _text = text
        
        self.keyboardType = keyboardType
        self.isPasswordField = isPasswordField
        
        self.focusOnStart = focusOnStart
        self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
        self._focusTextField = focusTextField
        
        self.textAlignment = textAlignment
        self.isUserInputEnabled = isUserInputEnabled
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
        self.textChanged = textChanged
    }
    

    func makeUIView(context: UIViewRepresentableContext<UIKitTextField>) -> UITextField {
        let textField = UITextField(frame: .zero)
        
        textField.placeholder = placeholder.localize()
        
        textField.keyboardType = keyboardType
        textField.isSecureTextEntry = isPasswordField
        
        textField.delegate = context.coordinator
        
        // set tag on all TextFields to be able to focus next view (= next tag). See Coordinator for more details
        Self.NextTagId = Self.NextTagId + 1 // unbelievable, there's no ++ operator
        textField.tag = Self.NextTagId
        
        if focusOnStart {
            textField.focus()
        }
        
        textField.textAlignment = textAlignment
        
        return textField
    }

    func updateUIView(_ uiView: UITextField, context: UIViewRepresentableContext<UIKitTextField>) {
        uiView.text = text
        
        if focusTextField {
            uiView.focus()
            
            DispatchQueue.main.async {
                self.focusTextField = false // reset value so that it can be set again (otherwise it may never gets resetted and then updateUIView() requests focus even though already another view got the focus in the meantime)
            }
        }
    }
    

    func makeCoordinator() -> UIKitTextField.Coordinator {
        return Coordinator(text: $text, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress, isUserInputEnabled: isUserInputEnabled,
                           actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
    }


     class Coordinator: NSObject, UITextFieldDelegate {

        @Binding private var text: String
        
        private var focusNextTextFieldOnReturnKeyPress: Bool
        
        private var isUserInputEnabled: Bool
        
        private var actionOnReturnKeyPress: (() -> Bool)?
        
        private var textChanged: ((String) -> Void)?


        init(text: Binding<String>, focusNextTextFieldOnReturnKeyPress: Bool, isUserInputEnabled: Bool,
             actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
            _text = text
            
            self.focusNextTextFieldOnReturnKeyPress = focusNextTextFieldOnReturnKeyPress
            self.isUserInputEnabled = isUserInputEnabled
            
            self.actionOnReturnKeyPress = actionOnReturnKeyPress
            
            self.textChanged = textChanged
        }
        
        
        func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
            return isUserInputEnabled
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
                
                didHandleReturnKey = nextView?.focus() ?? false
            }

            if didHandleReturnKey == false {
                textField.clearFocus() // default behaviour
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
