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
    @State private var trailingNavigationBarItem: AnyView? = nil
    
    @State private var showNewOptionsActionSheet = false
    
    @State private var selectedNewOption: Int? = nil
    
    
    @Inject private var presenter: BankingPresenterSwift
    
 
    @ViewBuilder
    var body: some View {
        if data.hasAtLeastOneAccountBeenAdded == false {
            AccountsTab(data: data)
            .hideNavigationBar()
        }
        else {
            TabView(selection: selectedTabBinding) {
                
                /*          First tab: Accounts         */
                
                AccountsTab(data: data)
                .onAppear {
                    self.savelySetAccountsTabNavigationBar()
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
                    NavigationLink(destination: LazyView(AddAccountDialog()), tag: 1, selection: self.$selectedNewOption.didSet(self.selectedNewOptionChanged)) {
                        EmptyView()
                    }
                    
                    NavigationLink(destination: LazyView(TransferMoneyDialog()), tag: 2, selection: self.$selectedNewOption.didSet(self.selectedNewOptionChanged)) {
                        EmptyView()
                    }
                        
                    .actionSheet(isPresented: self.$showNewOptionsActionSheet, content: {
                       self.generateNewActionSheet()
                    })
                }
                .onAppear {
                    self.resetNavigationBar()
                }
                .tabItem {
                    VStack {
                        Image(systemName: "plus.circle.fill")
                        Text("New")
                    }
                }
                .tag(Self.OverlayTabIndex)
                

                /*          Third tab: Settings dialog       */

                SettingsDialog(data: data)
                .onAppear {
                    self.savelySetSettingsTabNavigationBar()
                }
                .tabItem {
                    VStack {
                        Image("gear.fill")
                        Text("Settings")
                    }
                }
                .tag(2)
                
            }
            .showNavigationBarTitle(LocalizedStringKey(navigationBarTitle))
            .navigationBarItems(leading: leadingNavigationBarItem, trailing: trailingNavigationBarItem)
        }
    }
    
    
    private func generateNewActionSheet() -> ActionSheet {
        var buttons = [ActionSheet.Button]()

        if data.hasAccountsThatSupportTransferringMoney {
            buttons.append(.default(Text("Show transfer money dialog")) { self.selectedNewOption = 2 })
        }
        
        return ActionSheet(
            title: Text("New ..."),
            buttons: buttons + [
                .default(Text("Add account")) { self.selectedNewOption = 1 },
                .cancel { self.showPreviousSelectedTab() }
            ]
        )
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
        let leadingItem = data.hasAtLeastOneAccountBeenAdded == false ? nil : AnyView(UpdateButton { _ in
            self.presenter.updateAccountsTransactionsAsync { _ in }
        })
        
        savelySetNavigationBar("Accounts", leadingItem)
    }
    
    private func savelySetSettingsTabNavigationBar() {
        savelySetNavigationBar("Settings", nil, nil)
    }
    
    private func savelySetNavigationBar(_ title: String, _ leadingNavigationBarItem: AnyView? = nil, _ trailingNavigationBarItem: AnyView? = nil) {
        setNavigationBar(title, leadingNavigationBarItem, trailingNavigationBarItem)
        
        DispatchQueue.main.async { // when pressing 'Cancel' on ActionSheet navigation bar has to be set asynchronously (why, SwiftUI?)
            self.setNavigationBar(title, leadingNavigationBarItem, trailingNavigationBarItem)
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) { // can't believe it, sometimes even DispatchQueue.main.async() doesn't work. Ok, so let's do it 1 second later again, then it works
            self.setNavigationBar(title, leadingNavigationBarItem, trailingNavigationBarItem)
        }
    }
    
    private func setNavigationBar(_ title: String, _ leadingNavigationBarItem: AnyView? = nil, _ trailingNavigationBarItem: AnyView? = nil) {
        // due to a SwiftUI bug this cannot be set in AccountsTab directly, so i have to do it via the indirection of navigationBarTitle property
        self.navigationBarTitle = title
        
        self.leadingNavigationBarItem = leadingNavigationBarItem
        self.trailingNavigationBarItem = trailingNavigationBarItem
    }
    
    private func resetNavigationBar() {
        self.setNavigationBar("", nil, nil)
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
