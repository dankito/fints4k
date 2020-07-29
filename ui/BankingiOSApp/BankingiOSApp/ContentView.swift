import SwiftUI
import BankingUiSwift


struct ContentView: View {
    
    static private let OverlayTabIndex = 1
    
    
    @ObservedObject var data: AppData = AppData()
    
    @State private var previousSelectedTab: Int = 0
    
    @State private var selectedTab = 0
    
    private var selectedTabBinding: Binding<Int> {
        Binding<Int>(
            get: { self.selectedTab },
            set: {
                if $0 == Self.OverlayTabIndex {
                    self.previousSelectedTab = self.selectedTab
                    self.showNewOptionsActionSheet = true
                }

                self.selectedTab = $0
        })
    }
    
    @State private var navigationBarTitle = ""
    
    @State private var leadingNavigationBarItem: AnyView? = nil
    
    @State private var showNewOptionsActionSheet = false
    
    @State private var selectedNewOption: Int? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
 
    var body: some View {
        TabView(selection: selectedTabBinding) {
            
            /*          First tab: Accounts         */
            
            AccountsTab(data: data)
            .onAppear {
                self.savelySetAccountsTabNavigationBar()
            }
            .onDisappear {
                self.navigationBarTitle = ""
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
                NavigationLink(destination: TransferMoneyDialog(), tag: 1, selection: self.$selectedNewOption.didSet(self.selectedNewOptionChanged)) {
                    EmptyView()
                }

                SheetPresenter(presentingSheet: $showNewOptionsActionSheet, content:
                    ActionSheet(
                        title: Text("New ..."),
                        buttons: [
                            .default(Text("Show transfer money dialog")) { self.selectedNewOption = 1 },
                            .cancel { self.showPreviousSelectedTab() }
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
            .tag(Self.OverlayTabIndex)
            
        }
        .navigationBarHidden(false)
        .navigationBarTitle(navigationBarTitle)
        .navigationBarItems(leading: leadingNavigationBarItem)
    }
    
    
    private func selectedNewOptionChanged(oldValue: Int?, newValue: Int?) {
        if newValue == nil && oldValue != nil {
            showPreviousSelectedTab()
        }
    }
    
    private func showPreviousSelectedTab() {
        self.selectedTab = self.previousSelectedTab
    }
    
    private func savelySetAccountsTabNavigationBar() {
        setAccountsTabNavigationBar()
        
        DispatchQueue.main.async { // when pressing 'Cancel' on ActionSheet navigation bar has to be set asynchronously (why, SwiftUI?)
            self.setAccountsTabNavigationBar()
        }
    }
    
    private func setAccountsTabNavigationBar() {
        // due to a SwiftUI bug this cannot be set in AccountsTab directly, so i have to do it here
        self.navigationBarTitle = "Accounts"
        
        self.leadingNavigationBarItem = AnyView(UpdateButton { _ in
            self.presenter.updateAccountsTransactionsAsync { _ in }
        })
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
