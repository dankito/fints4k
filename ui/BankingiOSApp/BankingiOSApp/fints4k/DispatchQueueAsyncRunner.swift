import Foundation
import MultiplatformUtils
import BankingUiSwift


class DispatchQueueAsyncRunner : IAsyncRunner {
    
    func runAsync(runnable: @escaping () -> Void) {
        let frozen = FreezerKt.freeze(obj: runnable)
        
        runnable()

        DispatchQueue(label: "DispatchQueueAsyncRunner", qos: .background).async {
            runnable()
        }
    }
    
    
}
