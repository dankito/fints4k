import SwiftUI
import BankingUiSwift


struct FlickerCodeTanView: View {
    
    private static let FlickerCodeScaleFactorUserDefaultsKey = "FlickerCodeScaleFactor"
    private static let FlickerCodeFrequencyDefaultsKey = "FlickerCodeFrequency"
    
    private static let SpaceBetweenStripesStepSize: CGFloat = 0.5
    private static let StripesWidthStepSize: CGFloat = 2.35 * Self.SpaceBetweenStripesStepSize
    
    
    private let MinScaleFactor: CGFloat = 10
    private let MaxScaleFactor: CGFloat
    
    
    private var tanChallenge: BankingUiSwift.FlickerCodeTanChallenge
    
    @State private var showBit1: Bool = true
    @State private var showBit2: Bool = true
    @State private var showBit3: Bool = true
    @State private var showBit4: Bool = true
    @State private var showBit5: Bool = true
    
    private let animator: FlickerCodeAnimator = FlickerCodeAnimator()
    
    
    @State private var frequency = CGFloat(UserDefaults.standard.float(forKey: Self.FlickerCodeFrequencyDefaultsKey, defaultValue: Float(FlickerCodeAnimator.DefaultFrequency)))
    
    private var frequencyBinding: Binding<CGFloat> {
        Binding<CGFloat>(
            get: { self.frequency },
            set: {
                if (self.frequency != $0) {
                    let newFrequency = $0
                    
                    self.animator.setFrequency(frequency: Int(newFrequency))
                    
                    UserDefaults.standard.set(newFrequency, forKey: Self.FlickerCodeFrequencyDefaultsKey)
                    
                    DispatchQueue.main.async {
                        self.frequency = newFrequency
                    }
                }
        })
    }
    
    
    @State private var stripeWidth: CGFloat = 24
    @State private var spaceBetweenStripes: CGFloat = 10
    @State private var spaceBetweenTanGeneratorPositionMarker: CGFloat = 4 * 40 + 4 * 15 - TanGeneratorPositionMarker.Width
    
    @State private var scaleFactor = CGFloat(UserDefaults.standard.float(forKey: Self.FlickerCodeScaleFactorUserDefaultsKey, defaultValue: 20.0))
    
    private var scaleFactorBinding: Binding<CGFloat> {
        Binding<CGFloat>(
            get: { self.scaleFactor },
            set: {
                if (self.scaleFactor != $0) {
                    let newFlickerScaleFactor = $0
                    
                    UserDefaults.standard.set(newFlickerScaleFactor, forKey: Self.FlickerCodeScaleFactorUserDefaultsKey)
                    
                    DispatchQueue.main.async {
                        self.scaleFactor = newFlickerScaleFactor
                        
                        self.calculateStripeWidth()
                    }
                }
        })
    }
    
    
    init(_ tanChallenge: BankingUiSwift.FlickerCodeTanChallenge) {
        self.tanChallenge = tanChallenge
        
        let oneStepDiff = 5 * Self.StripesWidthStepSize + 4 * Self.SpaceBetweenStripesStepSize
        MaxScaleFactor = CGFloat(Int(UIScreen.main.bounds.width / oneStepDiff))
        
        animator.setFrequency(frequency: Int(frequency))
    }
    
    
    var body: some View {
        Section {
            HStack {
                Text("Tan Generator Frequency")
                
                Spacer()
                
                Image(systemName: "tortoise")
                
                Slider(value: frequencyBinding, in: CGFloat(FlickerCodeAnimator.MinFrequency)...CGFloat(FlickerCodeAnimator.MaxFrequency), step: 1)
                
                Image(systemName: "hare")
            }
            
            ScaleImageView(scaleFactorBinding, imageMinWidth: MinScaleFactor, imageMaxWidth: MaxScaleFactor, step: 1)
            
            VStack {
                HStack {
                    Spacer()
                    
                    TanGeneratorPositionMarker()
                    
                    Spacer()
                    .frame(width: spaceBetweenTanGeneratorPositionMarker)
                    
                    TanGeneratorPositionMarker()
                    
                    Spacer()
                }
                .padding(.bottom, -4)
                
                HStack {
                    Spacer()

                    HStack {
                        FlickerCodeStripe($showBit1, $stripeWidth)
                        
                        Spacer()
                        .frame(width: spaceBetweenStripes)
                        
                        FlickerCodeStripe($showBit2, $stripeWidth)
                        
                        Spacer()
                        .frame(width: spaceBetweenStripes)
                        
                        FlickerCodeStripe($showBit3, $stripeWidth)
                        
                        Spacer()
                        .frame(width: spaceBetweenStripes)
                        
                        FlickerCodeStripe($showBit4, $stripeWidth)
                        
                        Spacer()
                        .frame(width: spaceBetweenStripes)
                        
                        FlickerCodeStripe($showBit5, $stripeWidth)
                    }
                    
                    Spacer()
                }
                .padding(.bottom, 12)
            }
            .background(Color.black)
            .listRowInsets(EdgeInsets())
        }
        // what a hack to be able to call animator.animate() (otherwise compiler would throw 'use of immutable self in closure' error)
        .executeMutatingMethod {
            self.calculateStripeWidth()
            
            self.animator.animate(self.tanChallenge.flickerCode.parsedDataSet, self.showStep)
        }
    }
    
    
    private func showStep(_ step: Step) {
        self.showBit1 = step.bit1.isHigh
        self.showBit2 = step.bit2.isHigh
        self.showBit3 = step.bit3.isHigh
        self.showBit4 = step.bit4.isHigh
        self.showBit5 = step.bit5.isHigh
    }
    
    
    private func calculateStripeWidth() {
        stripeWidth = scaleFactor * Self.StripesWidthStepSize
        spaceBetweenStripes = scaleFactor * Self.SpaceBetweenStripesStepSize
        
        spaceBetweenTanGeneratorPositionMarker = 4 * stripeWidth + 4 * spaceBetweenStripes - TanGeneratorPositionMarker.Width
    }
    
}


struct FlickerCodeTanView_Previews: PreviewProvider {

    static var previews: some View {
        FlickerCodeTanView(previewFlickerCodeTanChallenge)
    }

}
