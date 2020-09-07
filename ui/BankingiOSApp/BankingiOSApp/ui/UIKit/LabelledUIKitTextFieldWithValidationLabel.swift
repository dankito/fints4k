import SwiftUI
import BankingUiSwift


// TODO: finish and then use it in TransferMoneyDialog
struct LabelledUIKitTextFieldWithValidationLabel: View {
    
    let label: LocalizedStringKey
    
    @Binding var text: String
    
    var placeholder: String = ""
    
    var keyboardType: UIKeyboardType = .default
    var isPasswordField: Bool = false
    
    var focusOnStart = false
    var focusNextTextFieldOnReturnKeyPress = false
    
    @State var focusTextField: Bool = false
    
    var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    var textChanged: ((String) -> Void)? = nil
    
    var validateInput: ((String) -> ValidationResult)? = nil
    
    
//    @State private var validationResult: ValidationResult? = nil
    @State private var validationResult: ValidationResult? = ValidationResult(inputString: "", validationSuccessful: false, didCorrectString: true, correctedInputString: "Hallo", validationError: "Unzulaessiges Zeichen eingegeben: $", validationHint: nil)
//    var validationError: Binding<String?> = .constant(nil)
    
    @State private var didJustCorrectValue = false
    

    var body: some View {
        VStack {
            Spacer()
            
            LabelledUIKitTextField(label: label, text: $text, focusOnStart: focusOnStart, focusNextTextFieldOnReturnKeyPress: focusNextTextFieldOnReturnKeyPress, actionOnReturnKeyPress: actionOnReturnKeyPress) { newValue in
                        self.enteredValueChanged(newValue)
            }
            .padding(.horizontal, 16)
            
            Spacer()
            
            validationResult.map { validationResult in
                ValidationLabel(validationResult)
            }
        }
        .listRowInsets(EdgeInsets())
    }
    
    
    private func enteredValueChanged(_ newValue: String) {
        if (didJustCorrectValue == false) {
            if let validationResult = validateInput?(newValue) {
                self.validationResult = validationResult.didCorrectString || validationResult.validationSuccessful == false ? validationResult :  nil
                
                if (validationResult.didCorrectString) {
                    didJustCorrectValue = true
                    
                    self.text = validationResult.correctedInputString
                }
            }
        }
        else {
            didJustCorrectValue = false
        }
        
        textChanged?(newValue)
    }

}


struct LabelledUIKitTextFieldWithValidationLabel_Previews: PreviewProvider {

    static var previews: some View {
        Form {
            Section {
                LabelledUIKitTextFieldWithValidationLabel(label: "Label", text: .constant("Text"))
            }
        }
    }

}
