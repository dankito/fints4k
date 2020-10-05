import SwiftUI


struct LabelledUIKitTextField: View {
    
    let label: LocalizedStringKey
    
    @Binding var text: String
    
    var placeholder: String = ""
    
    var keyboardType: UIKeyboardType = .default
    var autocapitalizationType: UITextAutocapitalizationType = .sentences
    var addDoneButton: Bool = false
    
    var isPasswordField: Bool = false
    
    var focusOnStart = false
    var focusNextTextFieldOnReturnKeyPress = false
    
    var focusTextField: Binding<Bool> = .constant(false)
    
    var isFocusedChanged: ((Bool) -> Void)? = nil
    
    
    var isUserInputEnabled: Bool = true
    
    var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    var textChanged: ((String) -> Void)? = nil
    
    
    @State private var textFitsIntoAvailableSpace: Bool = true
    

    var body: some View {
        HStack(alignment: .center) {
            Text(label)
            .onTapGesture {
                DispatchQueue.main.async {
                    self.focusTextField.wrappedValue = true
                }
            }
            
            Spacer()

            if textFitsIntoAvailableSpace {
                textField
            }
            else {
                ScrollView(.horizontal) {
                    textField
                }
            }
        }
    }
    
    
    private var textField: some View {
        UIKitTextField(placeholder, text: $text,
            keyboardType: keyboardType, autocapitalizationType: autocapitalizationType, addDoneButton: addDoneButton,
            isPasswordField: isPasswordField,
            focusOnStart: focusOnStart, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress, focusTextField: focusTextField,
            isFocusedChanged: isFocusedChanged, textAlignment: .right, isUserInputEnabled: isUserInputEnabled,
            actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)

            .background(

                // Render the limited text and measure its size
                Text(text).lineLimit(1)
                    .background(GeometryReader { availableSpace in
                        Color.clear.onAppear {
                            let textsFrame = availableSpace.frame(in: .global)
                            self.textFitsIntoAvailableSpace = textsFrame.maxX <= UIScreen.main.bounds.width
                        }
                    })
                    .hidden() // Hide the background
        )
        .onTapGesture {
            DispatchQueue.main.async {
                self.focusTextField.wrappedValue = true
            }
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
