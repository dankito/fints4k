import SwiftUI
import CoreData


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
    
    // TODO: implement passing multiple arguments to localize()
//    func localize(_ arguments: CVarArg...) -> String {
//        return localize(arguments)
//    }
//
//    func localize(_ arguments: [CVarArg]) -> String {
//        return String(format: NSLocalizedString(self, comment: ""), arguments)
//    }
    
    func localize(_ arguments: CVarArg) -> String {
        return String(format: NSLocalizedString(self, comment: ""), arguments)
    }
    

    subscript(_ i: Int) -> String {
      let idx1 = index(startIndex, offsetBy: i)
      let idx2 = index(idx1, offsetBy: 1)
      return String(self[idx1..<idx2])
    }

    subscript (r: Range<Int>) -> String {
      let start = index(startIndex, offsetBy: r.lowerBound)
      let end = index(startIndex, offsetBy: r.upperBound)
      return String(self[start ..< end])
    }

    subscript (r: CountableClosedRange<Int>) -> String {
      let startIndex =  self.index(self.startIndex, offsetBy: r.lowerBound)
      let endIndex = self.index(startIndex, offsetBy: r.upperBound - r.lowerBound)
      return String(self[startIndex...endIndex])
    }
    
    
    ///
    /// Be aware that this crashes when used in a View's body method. So use it only e.g. in init() method
    ///
    /// This an htmlToString has been copied from https://stackoverflow.com/a/47230632
    ///
    var htmlToAttributedString: NSAttributedString? {
        guard let data = data(using: .utf8) else { return nil }
        do {
            return try NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding:String.Encoding.utf8.rawValue], documentAttributes: nil)
        } catch {
            return nil
        }
    }
    
    var htmlToString: String {
        return htmlToAttributedString?.string ?? ""
    }
    
    
    var isCoreDataId: Bool {
        return self.starts(with: "x-coredata://")
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


extension NSManagedObject {
    
    var objectIDAsString: String {
        return self.objectID.uriRepresentation().absoluteString
    }
    
}

extension NSManagedObjectContext {
    
    func objectByID<T : NSManagedObject>(_ objectID: String) -> T? {
        if objectID.isCoreDataId {
            if let url = URL(string: objectID) {
                if let managedObjectId = self.persistentStoreCoordinator?.managedObjectID(forURIRepresentation: url) {
                    let object = try? self.existingObject(with: managedObjectId) as? T
                    if object?.isFault == false {
                        return object
                    }
                }
            }
        }
        
        return nil
    }
    
}
