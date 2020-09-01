import SwiftUI


struct SectionWithRightAlignedEditButton<Content: View>: View {
    
    private let sectionTitle: LocalizedStringKey
    
    private let content: Content
    

    init(sectionTitle: LocalizedStringKey, @ViewBuilder content: () -> Content) {
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
