import SwiftUI


class Stopwatch {
    
    static func logDuration<T>(_ action: String, block: () -> T) -> T {
        let stopwatch = Stopwatch()
        
        let result = block()
        
        stopwatch.stopAndPrint(action)
        
        return result
    }
    
    
    private var startTime: DispatchTime? = nil
    
    var elapsed: UInt64? = nil
    
    
    init(_ createStarted: Bool = true) {
        if createStarted {
            start()
        }
    }
    
    
    func start() {
        startTime = now()
    }
    
    @discardableResult
    func stop() -> UInt64 {
        let endTime = now()
        
        let elapsed = endTime.uptimeNanoseconds - (startTime ?? endTime).uptimeNanoseconds
        
        self.elapsed = elapsed
        
        return elapsed
    }
    
    func stopAndPrint(_ action: String) {
        stop()
        
        self.print(action)
    }
    
    func print(_ action: String) {
        if let elapsed = elapsed {
            let totalMillis = elapsed / 1_000_000
            let millis = totalMillis % 1000
            let seconds = totalMillis / 1000
            
            Swift.print(String(format: "\(action) took %02d:%03d", seconds, millis))
        }
    }
    
    
    private func now() -> DispatchTime {
        return .now()
    }
    
}
