import SwiftUI


struct SectionWithRightAlignedEditButton<Content: View>: View {
    
    private let sectionTitle: String
    
    private let content: Content
    

    init(sectionTitle: String, @ViewBuilder content: () -> Content) {
        self.sectionTitle = sectionTitle
        self.content = content()
    }
    

    var body: some View {
        Section(header: EditButton().frame(maxWidth: .infinity, alignment: .trailing)
            .overlay(Text(sectionTitle), alignment: .leading)) {
                content
        }
    }

}


struct SectionWithRightAlignedEditButton_Previews: PreviewProvider {

    static var previews: some View {
        SectionWithRightAlignedEditButton(sectionTitle: "Section") {
            Text("Body")
        }
    }

}
