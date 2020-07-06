
import SwiftUI

struct AccountsTab: View {
    var body: some View {
        NavigationView {
            NavigationLink(destination: AddAccountDialog()) {
                Text("Add account")
            }
        }
        .navigationBarHidden(true)
    }
}


struct AccountsTab_Previews: PreviewProvider {
    static var previews: some View {
        AccountsTab()
    }
}