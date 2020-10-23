import SwiftUI
import AVFoundation
import BankingUiSwift


class TabBarController : UITabBarController, UITabBarControllerDelegate {
    
    @ObservedObject var data: AppData = AppData()
    
    private let presenter: BankingPresenterSwift
    
    
    init(_ presenter: BankingPresenterSwift) {
        self.presenter = presenter
        
        super.init(nibName: nil, bundle: Bundle.main)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
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
        let moneyTransferFromQrCodeAction = UIAlertAction.default("Money transfer from scanning QR-Code".localize()) { self.scanQrCodeIfCameraAccessGranted() }
        moneyTransferFromQrCodeAction.isEnabled = data.hasAccountsThatSupportTransferringMoney
        
        let transferMoneyAction = UIAlertAction.default("Show transfer money dialog".localize()) { self.presenter.showTransferMoneyDialog(preselectedValues: nil) }
        transferMoneyAction.isEnabled = data.hasAccountsThatSupportTransferringMoney
        
        ActionSheet(
            nil,
            moneyTransferFromQrCodeAction,
            transferMoneyAction,
            UIAlertAction.default("Add account") { SceneDelegate.navigateToView(AddAccountDialog()) },
            UIAlertAction.cancel()
        ).show(self.tabBar, self.tabBar.bounds.midX, 0)
    }
    
    private func scanQrCodeIfCameraAccessGranted() {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            if granted {
                DispatchQueue.main.async { // completionHandler is called on an arbitrary dispatch queue
                    SceneDelegate.navigateToViewController(ScanQrCodeViewController() { decodedQrCode in
                        if let decodedQrCode = decodedQrCode {
                            self.presenter.showTransferMoneyDialogWithDataFromQrCode(decodedQrCode: decodedQrCode)
                        }
                    })
                }
            }
        }
    }
    
}
