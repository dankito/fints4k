import SwiftUI

struct ContentView: View {
    
    @ObservedObject var data: AppData = AppData()
    
    @State private var selection = 0
 
    var body: some View {
        TabView(selection: $selection) {
            AccountsTab(data: data)
                .tabItem {
                    VStack {
                        Image("first")
                        Text("Accounts")
                    }
                }
                .tag(0)

            NavigationView {
                VStack {
                    NavigationLink(destination: TransferMoneyDialog()) {
                        Text("Show transfer money dialog")
                    }
                }
            }
                .tabItem {
                    VStack {
                        Image("second")
                        Text("Second")
                    }
                }
                .tag(1)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
