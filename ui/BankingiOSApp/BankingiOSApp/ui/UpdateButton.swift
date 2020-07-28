import SwiftUI


struct UpdateButton: View {
    
    private let action: (Any?) -> Void
    
    private let actionParameter: Any?
    
    
    init(_ action: @escaping (Any?) -> Void) {
        self.init(actionParameter: nil, action)
    }
    
    init(actionParameter: Any? = nil, _ action: @escaping (Any?) -> Void) {
        self.action = action
        
        self.actionParameter = actionParameter
    }
    
    
    var body: some View {
        Button(
            action: { self.action(self.actionParameter) },
            label: { Image(systemName: "arrow.2.circlepath") }
        )
    }
}


struct UpdateButton_Previews: PreviewProvider {
    static var previews: some View {
        UpdateButton( { _ in } )
    }
}
