import SwiftUI


struct SectionHeaderWithRightAlignedEditButton: View {
    
    private let sectionTitle: LocalizedStringKey
    
    private let isEditButtonEnabled: Bool
    
    
    init(_ sectionTitle: LocalizedStringKey, isEditButtonEnabled: Bool = true) {
        self.sectionTitle = sectionTitle

        self.isEditButtonEnabled = isEditButtonEnabled
    }
    

    var body: some View {
        EditButton()
            .alignVertically(.trailing)
            .disabled( !isEditButtonEnabled)
            .overlay(Text(sectionTitle), alignment: .leading)
    }

}


struct SectionHeaderWithRightAlignedEditButton_Previews: PreviewProvider {

    static var previews: some View {
        SectionHeaderWithRightAlignedEditButton("Section")
    }

}
