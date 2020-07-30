import SwiftUI


class Favicon {
    
    let url: String
    
    let iconType: FaviconType
    
    let size: Size?
    
    let type: String?
    
    
    init(url: String, iconType: FaviconType, size: Size? = nil, type: String? = nil) {
        self.url = url
        self.iconType = iconType

        self.size = size
        self.type = type
    }
    
}
