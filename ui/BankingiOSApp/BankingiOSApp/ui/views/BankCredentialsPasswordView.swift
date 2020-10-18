import SwiftUI


struct BankCredentialsPasswordView: View {
    
    @Binding private var password: String
    
    @Binding private var savePassword: Bool
    
    private var handleReturnKeyPress: (() -> Bool)? = nil
    
    
    init(_ password: Binding<String>, _ showPassword: Binding<Bool>, _ handleReturnKeyPress: (() -> Bool)? = nil) {
        self._password = password
        self._savePassword = showPassword
        self.handleReturnKeyPress = handleReturnKeyPress
    }
    

    @ViewBuilder
    var body: some View {
        LabelledUIKitTextField(label: "Online banking login password", text: $password, placeholder: "Enter Online banking login password",
                               autocapitalizationType: .none, isPasswordField: true, actionOnReturnKeyPress: handleReturnKeyPress)
        
        Toggle("Save online banking login password", isOn: $savePassword)
            .disabled(true)
    }

}


struct BankCredentialsPasswordView_Previews: PreviewProvider {

    static var previews: some View {
        BankCredentialsPasswordView(.constant(""), .constant(true))
    }

}
