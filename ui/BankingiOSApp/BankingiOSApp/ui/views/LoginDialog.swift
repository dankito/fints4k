import SwiftUI


struct LoginDialog: View {
    
    private let authenticationService: AuthenticationService
    
    private let allowCancellingLogin: Bool
    
    private let loginReason: LocalizedStringKey?
    
    private let loginResult: (Bool) -> Void
    
    
    @State private var enteredPassword: String = ""
    
    
    init(_ authenticationService: AuthenticationService = AuthenticationService(), allowCancellingLogin: Bool = false, loginReason: LocalizedStringKey? = nil, loginResult: @escaping (Bool) -> Void) {
        
        self.authenticationService = authenticationService
        self.allowCancellingLogin = allowCancellingLogin
        self.loginReason = loginReason
        self.loginResult = loginResult
        
        if authenticationService.needsBiometricAuthenticationToUnlockApp {
            self.loginWithBiometricAuthentication()
        }
    }
    

    var body: some View {
        VStack {
            if authenticationService.needsFaceIDToUnlockApp {
                VStack {
                    Spacer()
                    
                    Text(loginReason ?? "To unlock app please authenticate with FaceID")
                        .multilineTextAlignment(.center)
                        .padding()
                        .padding(.bottom, 30)

                    FaceIDButton(50, self.loginWithBiometricAuthentication)
                    
                    Spacer()
                }
            }
            else if authenticationService.needsTouchIDToUnlockApp {
                VStack {
                    Spacer()
                    
                    Text(loginReason ?? "To unlock app please authenticate with TouchID")
                        .multilineTextAlignment(.center)
                        .padding()
                        .padding(.bottom, 35)

                    TouchIDButton(self.loginWithBiometricAuthentication)
                    
                    Spacer()
                }
            }
            else {
                loginReason.map { loginReason in
                    Text(loginReason)
                        .multilineTextAlignment(.center)
                        .padding()
                        .padding(.bottom, 0)
                }
                
                Form {
                    Section {
                        LabelledUIKitTextField(label: "Password", text: $enteredPassword, placeholder: "Enter your password", isPasswordField: true, focusOnStart: true,
                                               actionOnReturnKeyPress: { self.loginWithPasswordOnReturnKeyPress() })
                    }
                    
                    Section {
                        Button("Login") { self.loginWithPassword() }
                            .alignVertically(.center)
                    }
                }
            }
        }
        .showNavigationBarTitle("Login Dialog title")
        .navigationBarItems(leading: allowCancellingLogin == false ? nil : createCancelButton {
            self.closeDialogAndDispatchLoginResult(false)
        })
    }
    
    
    private func loginWithBiometricAuthentication() {
        authenticationService.loginWithBiometricAuthentication(self.handleAuthenticationResult)
    }
    
    private func loginWithPasswordOnReturnKeyPress() -> Bool {
        loginWithPassword()
        
        return true
    }
    
    private func loginWithPassword() {
        authenticationService.loginWithPassword(enteredPassword, self.handleAuthenticationResult)
    }
    
    private func handleAuthenticationResult(success: Bool, errorMessage: String?) {
        // as .alert() didn't work (why SwiftUI?), displaying Alert now manually
        if let errorMessage = errorMessage {
            UIAlert("Authentication failed", errorMessage, UIAlertAction.ok())
                .show()
        }
        else if success {
            closeDialogAndDispatchLoginResult(true)
        }
    }
    
    private func closeDialogAndDispatchLoginResult(_ authenticationSuccess: Bool) {
        SceneDelegate.rootNavigationController?.popViewController(animated: false)
        
        self.loginResult(authenticationSuccess)
    }

}


struct LoginDialog_Previews: PreviewProvider {

    static var previews: some View {
        LoginDialog { _ in }
    }

}
