import UIKit
import SwiftUI


struct UIKitImageView : UIViewRepresentable {

    typealias UIViewType = UIImageView
    
    let data: Data

    
    func makeUIView(context: Context) -> UIImageView {
        let imageView = UIImageView(image: UIImage(data: data))
        
        imageView.contentMode = .scaleAspectFit
        
        return imageView
    }
    
    func updateUIView(_ uiView: UIImageView, context: Context) {
        
    }
    
}
