
import SwiftUI


extension View {
    
    func hideNavigationBar() -> some View {
        return self
            .navigationBarHidden(true)
            .navigationBarTitle("Title")
    }
    
}