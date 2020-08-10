import SwiftUI


struct AddAccountButtonView: View {
    
    @State private var showAddAccountDialog = false
    

    var body: some View {
        VStack {
            HStack {
                Spacer()
                
                Button("Add account") { self.showAddAccountDialog = true }
                
                Spacer()
            }
            .padding(.top, 10) // to fix that hidden NavigationLink below pulls HStack up
            .frame(maxWidth: .infinity, minHeight: 40)
            

            NavigationLink(destination: LazyView(AddAccountDialog()), isActive: $showAddAccountDialog) {
                EmptyView()
            }
            .hidden()
            .frame(height: 0)
        }
    }

}


struct AddAccountButtonView_Previews: PreviewProvider {

    static var previews: some View {
        AddAccountButtonView()
    }

}
