import SwiftUI


class SelectorWrapper : NSObject {
    
    private let _action: () -> ()
    

    init(_ action: @escaping () -> ()) {
        _action = action
        super.init()
    }
    

    @objc func action() {
        _action()
    }
    
}
