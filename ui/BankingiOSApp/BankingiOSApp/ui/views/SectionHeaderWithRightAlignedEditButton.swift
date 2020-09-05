import SwiftUI


struct SectionHeaderWithRightAlignedEditButton: View {
    
    private let sectionTitle: LocalizedStringKey
    
    
    init(_ sectionTitle: LocalizedStringKey) {
        self.sectionTitle = sectionTitle
    }
    

    var body: some View {
        EditButton()
            .frame(maxWidth: .infinity, alignment: .trailing)
            .overlay(Text(sectionTitle), alignment: .leading)
    }

}


struct SectionHeaderWithRightAlignedEditButton_Previews: PreviewProvider {

    static var previews: some View {
        SectionHeaderWithRightAlignedEditButton("Section")
    }

}
