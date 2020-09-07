import Foundation
import MultiplatformUtils
import BankingUiSwift


class DispatchQueueAsyncRunner : IAsyncRunner {
    
    func runAsync(runnable: @escaping () -> Void) {
        DispatchQueue.main.async { // we cannot run runnable on a background thread due to Kotlin Native's memory model (that objects dispatched to other threads have to be immutable)
            runnable()
        }
    }
    
    
}
