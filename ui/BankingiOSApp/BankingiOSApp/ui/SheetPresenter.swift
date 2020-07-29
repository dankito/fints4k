import SwiftUI


struct SheetPresenter: View {
    
    @Binding var presentingSheet: Bool
    
    var content: ActionSheet
    
    
    var body: some View {
        Text("")
            .actionSheet(isPresented: self.$presentingSheet, content: { self.content })
    }
}
