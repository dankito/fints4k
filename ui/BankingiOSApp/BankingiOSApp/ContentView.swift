import SwiftUI
import BankingUiSwift


struct ContentView: View {
    
    @ObservedObject var data: AppData = AppData()
    
    @State private var selection = 0
    
    @State private var navigationBarTitle = ""
    
    @State private var leadingNavigationBarItem: AnyView? = nil
    
    @State private var showTransferMoneyOptionsActionSheet = false
    @State private var selectedTransferMoneyOption: Int? = 0
    
    
    @Inject private var presenter: BankingPresenterSwift
    
 
    var body: some View {
        TabView(selection: $selection) {
            
            /*          First tab: Accounts         */
            
            AccountsTab(data: data)
            .onAppear {
                // due to a SwiftUI bug this cannot be set in AccountsTab directly, so i have to do it here
                self.navigationBarTitle = "Accounts"
                self.leadingNavigationBarItem = AnyView(UpdateButton { _ in
                    self.presenter.updateAccountsTransactionsAsync { _ in }
                })
            }
            .onDisappear {
                self.leadingNavigationBarItem = nil
            }
            .tabItem {
                VStack {
                    Image("accounts")
                    Text("Accounts")
                }
            }
            .tag(0)

            /*          Second tab: 'New' action sheet button       */
            
            VStack {
                NavigationLink(destination: TransferMoneyDialog()) {
                    Text("Show transfer money dialog")
                }
                
                NavigationLink(destination: TransferMoneyDialog().onDisappear(perform: {
                    NSLog("Disappearing NavigationLink") // TODO: remove
                    self.selectedTransferMoneyOption = 0
                }), tag: 1, selection: $selectedTransferMoneyOption) {
                    EmptyView()
                }

                SheetPresenter(presentingSheet: $showTransferMoneyOptionsActionSheet, content:
                    ActionSheet(
                        title: Text("New ..."),
                        buttons: [
                            .default(Text("Show transfer money dialog")) { self.selectedTransferMoneyOption = 1 },
                            .cancel()
                        ]
                    )
                )
            }
            .tabItem {
                VStack {
                    Image(systemName: "plus.circle.fill")
                    Text("New")
                }
            }
            .tag(1)
            
        }
        .navigationBarHidden(false)
        .navigationBarTitle(navigationBarTitle)
        .navigationBarItems(leading: leadingNavigationBarItem)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
