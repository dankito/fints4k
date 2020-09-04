import SwiftUI


class InterceptTabClickViewController : UIViewController {
    
    var tabClicked: () -> Void = { }
    
    
    convenience init(_ tabClicked: @escaping () -> Void) {
        self.init()
        
        self.tabClicked = tabClicked
    }
    
}
