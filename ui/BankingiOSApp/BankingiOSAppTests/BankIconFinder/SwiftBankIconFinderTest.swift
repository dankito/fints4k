import XCTest
@testable import Bankmeister


class SwiftBankIconFinderTest: XCTestCase {
    
    private let underTest = SwiftBankIconFinder()
    

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }
    

    func testBerlinerSparkasse() throws {
        
        // when
        let result = underTest.findBankWebsite(bankName: "Sparkasse Berlin")
        
        // then
        XCTAssertNotNil(result)
        XCTAssertEqual(result!, "https://www.berliner-sparkasse.de")
    }

}
