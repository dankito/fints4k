import SwiftUI


class TabBarController : UITabBarController, UITabBarControllerDelegate {
    
    @ObservedObject var data: AppData = AppData()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.delegate = self
        
        setupTabs()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }

    
    private func setupTabs() {
        let accountsTab = buildControllerAndTabBarItem("Accounts", "accounts", AccountsDialog(data: data))
        
        
        let newOptionsTab = InterceptTabClickViewController { self.showNewOptionsActionSheet() }
        newOptionsTab.tabBarItem = buildTabBarItem("New", "new")
        
        
        let settingsTab = buildControllerAndTabBarItem("Settings",  "Settings", LazyView(SettingsDialog(self.data)))
        
        
        self.viewControllers = [accountsTab, newOptionsTab, settingsTab]
        
        
        if let firstViewController = viewControllers?.first {
            DispatchQueue.main.async { // wait till views are created before setting their title and navigation bar items
                self.setNavigationBarForViewController(firstViewController)
            }
        }
    }
         
    private func buildControllerAndTabBarItem<Content: View>(_ title: String, _ imageName: String, _ view: Content) -> UIViewController {
        return buildControllerAndTabBarItem(title, UIImage(named: imageName), view)
    }
     
    private func buildControllerAndTabBarItem<Content: View>(_ title: String, _ image: UIImage? = nil, _ view: Content) -> UIViewController {
        let localizedTitle = title.localize()
        
        let tabController = UIHostingController(rootView: view)
        tabController.title = localizedTitle
        
        tabController.tabBarItem = buildTabBarItem(localizedTitle, image)
        
        return tabController
    }
     
    private func buildTabBarItem(_ title: String, _ imageName: String) -> UITabBarItem {
        return buildTabBarItem(title.localize(), UIImage(named: imageName))
    }
     
    private func buildTabBarItem(_ localizedTitle: String, _ image: UIImage? = nil) -> UITabBarItem {
        return UITabBarItem(title: localizedTitle, image: image, selectedImage: nil)
    }
    
    
    func tabBarController(_ tabBarController: UITabBarController, didSelect viewController: UIViewController) {
        setNavigationBarForViewController(viewController)
    }
    
    func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {
        if viewController.isKind(of: InterceptTabClickViewController.self) {
            (viewController as! InterceptTabClickViewController).tabClicked()
            
            return false
        }
        
        return true
    }
    
    
    private func setNavigationBarForViewController(_ viewController: UIViewController) {
        self.title = viewController.title
        
        self.navigationItem.leftBarButtonItem = viewController.navigationItem.leftBarButtonItem
        self.navigationItem.rightBarButtonItem = viewController.navigationItem.rightBarButtonItem
    }
    
    
    private func showNewOptionsActionSheet() {
        let transferMoneyAction = UIAlertAction.default("Show transfer money dialog".localize()) { SceneDelegate.navigateToView(TransferMoneyDialog()) }
        transferMoneyAction.isEnabled = data.hasAccountsThatSupportTransferringMoney
        
        ActionSheet(
            nil,
            transferMoneyAction,
            UIAlertAction.default("Add account") { SceneDelegate.navigateToView(AddAccountDialog()) },
            UIAlertAction.cancel()
        ).show(self.tabBar, self.tabBar.bounds.midX, 0)
    }
    
}
