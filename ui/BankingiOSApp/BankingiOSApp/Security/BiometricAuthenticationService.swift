import LocalAuthentication


class BiometricAuthenticationService {
    
    private let localAuthenticationContext = LAContext()
    
    
    var biometryType: LABiometryType {
        localAuthenticationContext.biometryType
    }
    
    var isFaceIDSupported: Bool {
        biometryType == .faceID
    }
    
    var isTouchIDSupported: Bool {
        biometryType == .touchID
    }
    
    var isBiometricAuthenticationAvailable: Bool {
        var authorizationError: NSError?
        
        return localAuthenticationContext.canEvaluatePolicy(LAPolicy.deviceOwnerAuthenticationWithBiometrics, error: &authorizationError)
    }
    
    
    func authenticate(_ authenticationReason: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        localAuthenticationContext.evaluatePolicy(LAPolicy.deviceOwnerAuthentication, localizedReason: authenticationReason.localize()) { (success, evaluationError) in
            var errorMessage: String? = nil

            if let errorObj = evaluationError {
                let (showMessageToUser, untranslatedErrorMessage) = self.getErrorMessageKey(errorCode: errorObj._code)
                if showMessageToUser {
                    errorMessage = untranslatedErrorMessage.localize()
                }
            }
            
            DispatchQueue.main.async { // handler is called off main thread, so dispatch result back to main thread
                authenticationResult(success, errorMessage)
            }
        }
    }
    
    // copied from https://www.appsdeveloperblog.com/touch-id-or-face-id-authentication-in-swift/
    func getErrorMessageKey(errorCode: Int) -> (Bool, String) {
    
       switch errorCode {
           
       case LAError.authenticationFailed.rawValue:
           return (true, "Authentication was not successful, because user failed to provide valid credentials.")
           
       case LAError.appCancel.rawValue:
           return (true, "Authentication was canceled by application (e.g. invalidate was called while authentication was in progress).")
           
       case LAError.invalidContext.rawValue:
           return (true, "LAContext passed to this call has been previously invalidated.")
           
       case LAError.notInteractive.rawValue:
           return (true, "Authentication failed, because it would require showing UI which has been forbidden by using interactionNotAllowed property.")
           
       case LAError.passcodeNotSet.rawValue:
           return (true, "Authentication could not start, because passcode is not set on the device.")
           
       case LAError.systemCancel.rawValue:
           return (true, "Authentication was canceled by system (e.g. another application went to foreground).")
           
       case LAError.userCancel.rawValue:
           return (false, "Authentication was canceled by user (e.g. tapped Cancel button).")
           
       case LAError.userFallback.rawValue:
           return (false, "Authentication was canceled, because the user tapped the fallback button (Enter Password).")
           
       default:
           return (true, "Error code \(errorCode) not found")
       }
       
    }
    
}
