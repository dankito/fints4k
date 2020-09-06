import SwiftUI


struct ProtectAppSettingsDialog: View {
    
    @Environment(\.presentationMode) var presentation
    
    
    private let authenticationService = AuthenticationService()
    
    private let supportedAuthenticationTypes: [AuthenticationType]
    
    @State private var isFaceIDSelected: Bool = false
    
    @State private var isTouchIDSelected: Bool = false
    
    @State private var isPasswordSelected: Bool = false
    
    @State private var selectedAuthenticationTypeIndex = 0
    
    private var selectedAuthenticationTypeIndexBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedAuthenticationTypeIndex },
            set: {
                if (self.selectedAuthenticationTypeIndex != $0) { // only if authentication type really changed
                    self.selectedAuthenticationTypeIndex = $0
                    self.selectedAuthenticationTypeChanged(self.supportedAuthenticationTypes[$0])
                }
        })
    }
    
    
    @State private var newPassword: String = ""
    
    @State private var confirmedNewPassword: String = ""
    
    @State private var successfullyAuthenticatedWithBiometricAuthentication = false
    
    @State private var successfullyAuthenticatedWithPassword = false
    
    
    init() {
        var authenticationTypes = [AuthenticationType]()
        
        if authenticationService.deviceSupportsFaceID {
            authenticationTypes.append(.faceID)
        }
        if authenticationService.deviceSupportsTouchID {
            authenticationTypes.append(.touchID)
        }

        authenticationTypes.append(.password)
        
        self.supportedAuthenticationTypes = authenticationTypes
        
        
        let currentAuthenticationType = authenticationService.authenticationType
        if currentAuthenticationType == .faceID || (currentAuthenticationType != .password && authenticationService.deviceSupportsFaceID) {
            _isFaceIDSelected = State(initialValue: true)
            _selectedAuthenticationTypeIndex = State(initialValue: 0)
        }
        else if currentAuthenticationType == .touchID || (currentAuthenticationType != .password && authenticationService.deviceSupportsTouchID) {
            _isTouchIDSelected = State(initialValue: true)
            _selectedAuthenticationTypeIndex = State(initialValue: 0)
        }
        else {
            _isPasswordSelected = State(initialValue: true)
            _selectedAuthenticationTypeIndex = State(initialValue: supportedAuthenticationTypes.firstIndex(of: .password)!)
        }
    }
    

    var body: some View {
        Form {
            if supportedAuthenticationTypes.count > 1 {
                Section {
                    Picker("", selection: selectedAuthenticationTypeIndexBinding) {
                        ForEach(0..<supportedAuthenticationTypes.count) { index in
                            Text(self.supportedAuthenticationTypes[index].rawValue.firstLetterUppercased.localize())
                                .tag(index)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    .alignVertically(.center)
                }
            }
            
            if isFaceIDSelected {
                Section {
                    FaceIDButton(self.doBiometricAuthentication)
                        .alignVertically(.center)
                }
            }
            
            if isTouchIDSelected {
                Section {
                    TouchIDButton(self.doBiometricAuthentication)
                        .alignVertically(.center)
                }
            }
            
            if isPasswordSelected {
                Section {
                    LabelledUIKitTextField(label: "Password", text: $newPassword.didSet(self.enteredPasswordChanged), placeholder: "Enter new password", isPasswordField: true,
                                           focusOnStart: true, focusNextTextFieldOnReturnKeyPress: true, actionOnReturnKeyPress: handleReturnKeyPress)
                    
                    LabelledUIKitTextField(label: "Confirm password", text: $confirmedNewPassword.didSet(self.enteredPasswordChanged), placeholder: "Confirm new password",
                                           isPasswordField: true, actionOnReturnKeyPress: handleReturnKeyPress)
                }
            }
            
            Section {
                HStack {
                    Spacer()
                    
                    Button("OK") { self.setAuthenticationType() }
                        .alignVertically(.center)
                        .disabled( !self.authenticatedWithNewAuthenticationType)
                    
                    Spacer()
                }
                .frame(maxWidth: .infinity, minHeight: 40)
            }
        }
        .fixKeyboardCoversLowerPart()
        .navigationBarTitle("Protect App Settings Dialog title")
    }
    
    
    private func selectedAuthenticationTypeChanged(_ type: AuthenticationType) {
        isFaceIDSelected = false
        isTouchIDSelected = false
        isPasswordSelected = false
    
        if type == .faceID {
            isFaceIDSelected = true
        }
        else if type == .touchID {
            isTouchIDSelected = true
        }
        else {
            isPasswordSelected = true
        }
        
        if isPasswordSelected == false {
            UIApplication.hideKeyboard()
        }
    }
    
    private func doBiometricAuthentication() {
        authenticationService.loginWithBiometricAuthentication { success, errorMessage in
            self.successfullyAuthenticatedWithBiometricAuthentication = success
        }
    }
    
    private func enteredPasswordChanged(_ oldValue: String, _ newValue: String) {
        successfullyAuthenticatedWithPassword = newPassword.isNotBlank && newPassword == confirmedNewPassword
    }
    
    private var authenticatedWithNewAuthenticationType: Bool {
        ((isFaceIDSelected || isTouchIDSelected) && successfullyAuthenticatedWithBiometricAuthentication) ||
            (isPasswordSelected && successfullyAuthenticatedWithPassword)
    }

    func handleReturnKeyPress() -> Bool {
        if authenticatedWithNewAuthenticationType {
            self.setAuthenticationType()
            
            return true
        }
        
        return false
    }
    
    private func setAuthenticationType() {
        if isFaceIDSelected {
            authenticationService.setAuthenticationType(.faceID)
        }
        else if isTouchIDSelected {
            authenticationService.setAuthenticationType(.touchID)
        }
        else {
            authenticationService.setAuthenticationTypeToPassword(newPassword)
        }
        
        presentation.wrappedValue.dismiss()
    }

}


struct ProtectAppSettingsDIalog_Previews: PreviewProvider {

    static var previews: some View {
        ProtectAppSettingsDialog()
    }

}
