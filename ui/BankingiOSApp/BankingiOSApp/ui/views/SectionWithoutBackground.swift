import SwiftUI


struct SectionWithoutBackground<Content: View>: View {
    
    private let content: () -> Content
    
    
    init(@ViewBuilder _ content: @escaping () -> Content) {
        self.content = content
    }
    

    var body: some View {
        Section {
            content()
        }
        .frame(maxWidth: .infinity, minHeight: 44) // has to have at least a height of 44 (iOS 14; iOS 13: 40), otherwise a white line at bottom gets displayed
        .removeSectionBackground()
    }

}


struct SectionWithoutBackground_Previews: PreviewProvider {

    static var previews: some View {
        Form {
            SectionWithoutBackground {
                Text("Hello")
            }
        }
    }

}
