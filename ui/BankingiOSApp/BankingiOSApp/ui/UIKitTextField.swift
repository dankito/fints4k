import SwiftUI


struct UIKitTextField: UIViewRepresentable {

    @Binding private var text: String
    
    private var placeHolder: String
    
    private var keyboardType: UIKeyboardType = .default
    private var isPasswordField: Bool = false
    
    private var actionOnReturnKeyPress: (() -> Void)? = nil
    
    init(_ titleKey: String, text: Binding<String>, keyboardType: UIKeyboardType = .default, isPasswordField: Bool = false, actionOnReturnKeyPress: (() -> Void)? = nil) {
        self.placeHolder = titleKey
        _text = text
        
        self.keyboardType = keyboardType
        self.isPasswordField = isPasswordField
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
    }
    

    func makeUIView(context: UIViewRepresentableContext<UIKitTextField>) -> UITextField {
        let textField = UITextField(frame: .zero)
        
        textField.placeholder = placeHolder.localize()
        
        textField.keyboardType = keyboardType
        textField.isSecureTextEntry = isPasswordField
        
        textField.delegate = context.coordinator
        
        return textField
    }

    func makeCoordinator() -> UIKitTextField.Coordinator {
        return Coordinator(text: $text, actionOnReturnKeyPress: actionOnReturnKeyPress /*, nextResponder: $nextResponder, isResponder: $isResponder */)
    }

    func updateUIView(_ uiView: UITextField, context: UIViewRepresentableContext<UIKitTextField>) {
         uiView.text = text
    }


     class Coordinator: NSObject, UITextFieldDelegate {

        @Binding private var text: String
        
        private var actionOnReturnKeyPress: (() -> Void)?


        init(text: Binding<String>, actionOnReturnKeyPress: (() -> Void)? = nil) {
            _text = text
            
            self.actionOnReturnKeyPress = actionOnReturnKeyPress
        }

        func textFieldDidChangeSelection(_ textField: UITextField) {
            text = textField.text ?? ""
        }
        
        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            actionOnReturnKeyPress?()
            
            textField.resignFirstResponder()
            
            return actionOnReturnKeyPress != nil
        }

    }
    
}


struct UIKitTextView_Previews: PreviewProvider {

    @State static private var text = ""
    
    static var previews: some View {
        UIKitTextField("Test label", text: $text)
    }

}
