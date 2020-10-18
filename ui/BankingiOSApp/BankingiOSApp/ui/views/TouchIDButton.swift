import SwiftUI


struct TouchIDButton: View {
    
    private let action: () -> Void
    
    
    init(_ action: @escaping () -> Void) {
        self.action = action
    }
    

    var body: some View {
        Button("Authenticate with TouchID", action: self.action)
    }

}


struct TouchIDButton_Previews: PreviewProvider {

    static var previews: some View {
        TouchIDButton { }
    }

}
