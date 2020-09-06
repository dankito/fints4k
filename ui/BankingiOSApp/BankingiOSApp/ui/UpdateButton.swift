import SwiftUI


struct UpdateButton: View {
    
    private let action: (Any?, @escaping () -> Void) -> Void
    
    private let actionParameter: Any?
    
    @State private var isExecutingAction = false
    
    
    init(_ action: @escaping (Any, @escaping () -> Void?) -> Void) {
        self.init(actionParameter: nil, action)
    }
    
    init(actionParameter: Any? = nil, _ action: @escaping (Any?, @escaping () -> Void) -> Void) {
        self.action = action
        
        self.actionParameter = actionParameter
    }
    
    
    var body: some View {
        Button(
            action: { self.executeAction() },
            label: { Image(systemName: "arrow.2.circlepath") }
        )
        .disabled(isExecutingAction)
    }
    
    
    private func executeAction() {
        isExecutingAction = true
        
        action(self.actionParameter) {
            self.isExecutingAction = false
        }
    }
    
}


struct UpdateButton_Previews: PreviewProvider {
    static var previews: some View {
        UpdateButton( { _, _ in } )
    }
}
