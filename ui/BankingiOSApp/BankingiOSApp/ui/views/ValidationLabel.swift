import SwiftUI
import BankingUiSwift


struct ValidationLabel: View {
    
    private let validationErrorOrHint: String
    
    private let isHint: Bool
    
    
    init(_ validationError: String) {
        self.init(validationError, false)
    }
    
    init(_ validationResult: ValidationResult) {
        self.init(validationResult.validationError ?? validationResult.validationHint ?? "",
                  validationResult.validationError == nil && validationResult.validationHint != nil)
    }
    
    init(_ validationErrorOrHint: String, _ isHint: Bool) {
        self.validationErrorOrHint = validationErrorOrHint
        self.isHint = isHint
    }
    

    var body: some View {
        InfoLabel(validationErrorOrHint)
        .foregroundColor(isHint ? Color.yellow : Color.red)
    }

}


struct ValidationLabel_Previews: PreviewProvider {

    static var previews: some View {
        ValidationLabel("Invalid characters used")
    }

}
