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
        VStack {
            Spacer()
            .frame(height: 6)
            
            HStack {
                Text(validationErrorOrHint)
                .padding(.leading, 16)
                
                Spacer()
            }
            
            Spacer()
            .frame(height: 18)
        }
        .font(.callout)
        .foregroundColor(isHint ? Color.yellow : Color.red)
        .systemGroupedBackground()
        .listRowInsets(EdgeInsets())
    }

}


struct ValidationLabel_Previews: PreviewProvider {

    static var previews: some View {
        ValidationLabel("Invalid characters used")
    }

}
