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
    
    
    // TODO: remove again
    private let enterTanState: EnterTanState
    
    
    init() {
        let customer = Customer(bankCode: "", customerId: "", password: "", finTsServerAddress: "")
        let selectedTanProcedure = TanProcedure(displayName: "chipTAN optisch", type: .chiptanflickercode, bankInternalProcedureCode: "chipTAN optisch")
        customer.supportedTanProcedures = [
            TanProcedure(displayName: "App TAN", type: .apptan, bankInternalProcedureCode: "App TAN"),
            selectedTanProcedure,
            TanProcedure(displayName: "SMS TAN", type: .smstan, bankInternalProcedureCode: "SMS TAN")
        ]
        customer.selectedTanProcedure = selectedTanProcedure
        
        customer.tanMedia = [
            TanMedium(displayName: "EC-Karte mit Nummer 12345678", status: .available),
            TanMedium(displayName: "Handy mit Nummer 0170 / 12345678", status: .available)
        ]
        
        self.enterTanState = EnterTanState(customer, TanChallenge(messageToShowToUser: "Gib die TAN ein du faules Stueck!\nAber jetzt manchen wir mal eine richtig, richtig lange Nachricht daraus.\nMal schauen, ob mir so viel Quatsch ueberhaupt einfaellt (aber anscheinend scheine ich sehr kreativ zu sein).", tanProcedure: selectedTanProcedure)) { result in }
    }
    
 
    var body: some View {
//        NavigationView {
//            VStack {
                TabView(selection: $selection) {
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
                            Image("first")
                            Text("Accounts")
                        }
                    }
                    .tag(0)

        //            actionSheet(isPresented: $showTransferMoneyOptionsActionSheet) {
        //                ActionSheet(
        //                    title: Text("Action"),
        //                    message: Text("Available actions"),
        //                    buttons: [
        //                        .default(Text("Show transfer money dialog")) { self.selectedTransferMoneyOption = 1 },
        //                        .destructive(Text("Delete"))
        //                    ]
        //                )
        //            }

        //            TransferMoneyDialog()
        //                .tabItem {
        //                    NavigationLink(destination: TransferMoneyDialog(), tag: 1, selection: $selectedTransferMoneyOption) {
        //                        EmptyView()
        //                    }
        //                    VStack {
        //                        Image("second")
        //                        Text("Second")
        //                    }
        //                    .onTapGesture {
        //                        NSLog("Tapped on second item") // TODO: remove again
        //                        self.showTransferMoneyOptionsActionSheet = true
        //                    }
        //                }
    //                NavigationView {
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
                                    title: Text(""),
                                    buttons: [
                                        .default(Text("Show transfer money dialog")) { self.selectedTransferMoneyOption = 1 },
                                        .cancel()
                                    ]
                                )
                            )
                        }
    //                }
                        .tabItem {
                            VStack {
                                Image(systemName: "plus.circle.fill")
                            }
                        }
                        .tag(1)
                    
                    EnterTanDialog(self.enterTanState)
                    .tabItem {
                        VStack {
                            Text("EnterTanDialog")
                        }
                    }
                    .tag(3)
                    .navigationBarHidden(false)
                    .navigationBarTitle("Enter TAN")
                    
                }
            .navigationBarHidden(false)
            .navigationBarTitle(navigationBarTitle)
            .navigationBarItems(leading: leadingNavigationBarItem)
                
                
//            }
            //.hideNavigationBar()
            //.navigationViewStyle(StackNavigationViewStyle()) // see https://stackoverflow.com/questions/59338711/swiftui-bug-navigationview-and-list-not-showing-on-ipad-simulator-only
//        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
