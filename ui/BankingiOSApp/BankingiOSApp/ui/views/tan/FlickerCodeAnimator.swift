import SwiftUI
import BankingUiSwift


class FlickerCodeAnimator {

    static let MinFrequency = 2
    static let MaxFrequency = 40
    static let DefaultFrequency = 30

    private var currentFrequency: Int = DefaultFrequency

    private var isPaused = false

    private var currentStepIndex = 0
    
    private var steps: [Step] = []
    
    private var showStep: ((Step) -> Void)? = nil

    private var timer: Timer? = nil
    


    func animate(_ flickerCode: String, _ showStep: @escaping (Step) -> Void) {
        self.stop() // stop may still running previous animation
        
        self.steps = FlickerCodeStepsCalculator().calculateSteps(flickerCode: flickerCode)
        
        self.showStep = showStep
                
        self.start()
    }
    
    
    private func start() {
        currentStepIndex = 0
        
        let interval = 1.0 / Double(currentFrequency)
        
        timer = Timer.scheduledTimer(withTimeInterval: interval, repeats: true) { timer in
            self.calculateAndShowNextStep()
        }
    }

    private func calculateAndShowNextStep() {
        if isPaused == false {
            let nextStep = steps[currentStepIndex]
            
            self.showStep?(nextStep)

            currentStepIndex = currentStepIndex + 1
            
            if (currentStepIndex >= steps.count) {
                currentStepIndex = 0 // all steps shown, start again from beginning
            }
        }
    }

    func stop() {
        timer?.invalidate()
        
        timer = nil
    }

    func pause() {
        self.isPaused = true
    }

    func resume() {
        self.isPaused = false
    }


    func setFrequency(frequency: Int) {
        if frequency >= Self.MinFrequency && frequency <= Self.MaxFrequency {
            let isRunning = timer != nil
            
            stop()
            
            currentFrequency = frequency
            
            if isRunning {
                start()
            }
        }
    }
    
}
