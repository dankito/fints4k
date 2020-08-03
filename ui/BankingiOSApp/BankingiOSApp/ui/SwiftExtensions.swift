import SwiftUI


extension String {
    
    var isNotEmpty: Bool {
        return !isEmpty
    }
    
    var isBlank: Bool {
        return isEmpty || trimmingCharacters(in: .whitespaces).isEmpty
    }
    
    var isNotBlank: Bool {
        return !isBlank
    }
    
    
    func localize() -> String {
        return NSLocalizedString(self, comment: "")
    }
    
}

extension Optional where Wrapped == String {
    
    var isNullOrEmpty: Bool {
        return self?.isEmpty ?? true
    }
    
    var isNullOrBlank: Bool {
        return self?.isBlank ?? true
    }
    
}


extension NSDecimalNumber {
    
    func isPositive() -> Bool {
        return !isNegative()
    }
    
    func isNegative() -> Bool {
        return doubleValue < 0.0
    }
    
    
    static func ==(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> Bool {
        return lhs.compare(rhs) == .orderedSame
    }

    static func <(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> Bool {
        return lhs.compare(rhs) == .orderedAscending
    }

    static prefix func -(value: NSDecimalNumber) -> NSDecimalNumber {
        return value.multiplying(by: NSDecimalNumber(mantissa: 1, exponent: 0, isNegative: true))
    }

    static func +(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> NSDecimalNumber {
        return lhs.adding(rhs)
    }

    static func -(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> NSDecimalNumber {
        return lhs.subtracting(rhs)
    }

    static func *(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> NSDecimalNumber {
        return lhs.multiplying(by: rhs)
    }

    static func /(lhs: NSDecimalNumber, rhs: NSDecimalNumber) -> NSDecimalNumber {
        return lhs.dividing(by: rhs)
    }

    static func ^(lhs: NSDecimalNumber, rhs: Int) -> NSDecimalNumber {
        return lhs.raising(toPower: rhs)
    }
    
}


extension Array where Element == NSDecimalNumber {
    
    func sum() -> NSDecimalNumber {
        return self.reduce(NSDecimalNumber.zero, +)
    }
    
}


extension Array {
    
    var isNotEmpty: Bool {
        return !isEmpty
    }
    
}


extension NSObject {
    
    var className: String {
        return String(describing: type(of: self))
    }
    
}


extension URL {
    
    static func encoded(_ url: String) -> URL? {
        return URL(string: url.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? url)
    }
    
    
    var absoluteStringWithouthQuery: String {
        let absoluteString = self.absoluteString
        
        let indexOfQuestionMark = absoluteString.firstIndex(of: "?")
        
        if let index = indexOfQuestionMark {
            return String(absoluteString[..<index])
        }
        
        return absoluteString
    }
    
    var absoluteStringWithouthFragment: String {
        return absoluteStringWithouthFragment(self.absoluteString)
    }
    
    private func absoluteStringWithouthFragment(_ absoluteString: String) -> String {
        let indexOfHash = absoluteString.firstIndex(of: "#")
        
        if let index = indexOfHash {
            return String(absoluteString[..<index])
        }
        
        return absoluteString
    }
    
    var absoluteStringWithouthQueryAndFragment: String {
        let absoluteStringWithouthQuery = self.absoluteStringWithouthQuery
        
        return absoluteStringWithouthFragment(absoluteStringWithouthQuery)
    }
    
    var removeQueryAndFragment: URL {
        return URL(string: self.absoluteStringWithouthQueryAndFragment)!
    }
    
}
