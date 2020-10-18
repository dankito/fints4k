import SwiftUI


class Size : Comparable, CustomStringConvertible {
    
    
    let width: Int
    
    let height: Int
    
    
    init(width: Int, height: Int) {
        self.width = width
        self.height = height
    }
    
    
    var isSquare: Bool {
        return width == height
    }

    var displayText: String {
        return "\(width) x \(height)"
    }
    
    
    static func < (lhs: Size, rhs: Size) -> Bool {
        return lhs.width < rhs.width
            && lhs.height < rhs.height
    }
    
    static func == (lhs: Size, rhs: Size) -> Bool {
        return lhs.width == rhs.width
            && lhs.height == rhs.height
    }
    
    
    var description: String {
        return displayText
    }
    
}
