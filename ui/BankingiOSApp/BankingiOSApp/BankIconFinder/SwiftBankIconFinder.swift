import Foundation
import SwiftSoup
import BankingUiSwift
import MultiplatformUtils


class SwiftBankIconFinder : IBankIconFinder {

    static let SearchBankWebsiteBaseUrlQwant = "https://lite.qwant.com/?l=de&t=mobile&q="

    static let SearchBankWebsiteBaseUrlEcosia = "https://www.ecosia.org/search?q="

    static let SearchBankWebsiteBaseUrlDuckDuckGo = "https://duckduckgo.com/html/?q="


    //static let ReplaceGfRegex = Pattern.compile(" \\(Gf [\\w]+\\)").toRegex()


    private let log = LoggerFactory.Companion().getLogger(name: "BankIconFinder")


    private let webClient = UrlSessionWebClient()

    private let faviconFinder = FaviconFinder()

//    private let faviconComparator = FaviconComparator(webClient)
    
    
    func findIconForBankAsync(bankName: String, prefSize: Int32, result: @escaping (String?) -> Void) {
        DispatchQueue.global(qos: .background).async {
            let bestUrl = self.findIconForBank(bankName: bankName, prefSize: prefSize)
            
            DispatchQueue.main.async { // dispatch reuslt back to main thread as mutable BankingPresenter / result callback can only be used on the thread they have been created on
                result(bestUrl)
            }
        }
    }
    
    func findIconForBank(bankName: String, prefSize: Int32) -> String? {
        if let bankUrl = findBankWebsite(bankName: bankName) {
            if let bankHomepageResponse = webClient.get(bankUrl).body {
                if let document = try? SwiftSoup.parse(bankHomepageResponse) {
                    let favicons = faviconFinder.extractFavicons(document: document, url: bankUrl)

                    // TODO:
    //                faviconComparator.getBestIcon(favicons, prefSize, prefSize + 32, true)?.let { prefFavicon ->
    //                    return prefFavicon.url
    //                }
    //
    //                return faviconComparator.getBestIcon(favicons, 16)?.url

                    return favicons.first?.url
                }
            }
        }

        return nil
    }
    
    func findBankWebsite(bankName: String) -> String? {
        let adjustedBankName = bankName.replacingOccurrences(of: "-alt-", with: "")/*.replace(ReplaceGfRegex, "") */ // TODO

        if let url = findBankWebsiteWithQwant(adjustedBankName) {
            return url
        }

        print("Could not find bank website with Qwant for '\(bankName)'")

        if let url = findBankWebsiteWithEcosia(adjustedBankName) {
            return url
        }
        
        print("Could not find bank website with Ecosia for '\(bankName)'")

        if let url = findBankWebsiteWithDuckDuckGo(adjustedBankName) {
            return url
        }
        
        print("Could not find bank website with DuckDuckGo for '\(bankName)'")

        return nil
    }

    private func findBankWebsiteWithQwant(_ bankName: String) -> String? {
        return findBankWebsite(bankName, Self.SearchBankWebsiteBaseUrlQwant) { searchResponseDoc in
            findLinksInQwantWebpage(searchResponseDoc)
                ?? []
        }
    }
    
    private func findLinksInQwantWebpage(_ searchResponseDoc: Document) -> [String]? {
        return (try? searchResponseDoc.select(".url"))?
                .filter { (try? $0.select("span").first()) == nil }
                .map { (try? $0.text()) ?? "" }
                .filter { $0.isNotBlank }
    }

    private func findBankWebsiteWithEcosia(_ bankName: String) -> String? {
        return findBankWebsite(bankName, Self.SearchBankWebsiteBaseUrlEcosia) { searchResponseDoc in
            (try? searchResponseDoc.select(".js-result-url"))?
                .map { (try? $0.attr("href")) ?? "" }
                .filter { $0.isNotBlank }
                ?? []
        }
    }

    private func findBankWebsiteWithDuckDuckGo(_ bankName: String) -> String? {
        return findBankWebsite(bankName, Self.SearchBankWebsiteBaseUrlDuckDuckGo) { searchResponseDoc in
                (try? searchResponseDoc.select(".result__url"))?
                .map { (try? $0.attr("href")) ?? "" }
                .filter { $0.isNotBlank }
                ?? []
            }
    }

    private func findBankWebsite(_ bankName: String, _ searchBaseUrl: String, extractUrls: (Document) -> [String]) -> String? {
        let encodedBankName = bankName.replacingOccurrences(of: " ", with: "+")

        let exactSearchUrl = searchBaseUrl + "\"" + encodedBankName + "\""
        if let searchResponseDocument = getSearchResultForBank(exactSearchUrl) {
            if let bestUrl = findBestUrlForBank(bankName: bankName, unmappedUrls: extractUrls(searchResponseDocument)) {
                return bestUrl
            }
        }


        let searchUrl = searchBaseUrl + encodedBankName
        if let searchResponseDocument = getSearchResultForBank(searchUrl) {
            return findBestUrlForBank(bankName: bankName, unmappedUrls: extractUrls(searchResponseDocument))
        }


        return nil
    }

    private func getSearchResultForBank(_ searchUrl: String) -> Document? {
        let response = webClient.get(searchUrl)

        if let responseBody = response.body {
            return try? SwiftSoup.parse(responseBody)
        }

        return nil
    }


    private func findBestUrlForBank(bankName: String, unmappedUrls: [String]) -> String? {
        let urlCandidates = getUrlCandidates(unmappedUrls)
        let urlCandidatesWithoutUnlikely = urlCandidates.filter { isUnlikelyBankUrl(bankName: bankName, urlCandidate: $0) == false }

        let urlForBank = findUrlThatContainsBankName(bankName: bankName, urlCandidates: urlCandidatesWithoutUnlikely)

        // cut off stuff like 'filalsuche' etc., they most like don't contain as many favicons as main page
        return getMainPageForBankUrl(urlForBank: urlForBank, urlCandidates: urlCandidatesWithoutUnlikely) ?? urlForBank
    }

    private func getUrlCandidates(_ urls: [String?]) -> [String] {
        return urls.map { fixUrl($0) }.filter { $0 != nil} as? [String] ?? []
    }

    private func fixUrl(_ url: String?) -> String? {
        if let url = url, url.isNotBlank {
            let urlEncoded = url.replacingOccurrences(of: " ", with: "%20F")

            if urlEncoded.starts(with: "http") {
                return urlEncoded
            }
            else {
                return "https://" + urlEncoded
            }
        }

        return nil
    }

    private func findUrlThatContainsBankName(bankName: String, urlCandidates: [String]) -> String? {
        let bankNameParts = bankName.replacingOccurrences(of: ",", with: "")
            .replacingOccurrences(of: "-", with: " ") // to find 'Sparda-Bank' in 'sparda.de'
            .replacingOccurrences(of: "ä", with: "ae").replacingOccurrences(of: "Ä", with: "ae")
            .replacingOccurrences(of: "ö", with: "oe").replacingOccurrences(of: "Ö", with: "oe")
            .replacingOccurrences(of: "ü", with: "ue").replacingOccurrences(of: "Ü", with: "ue")
            .split(separator: " ").map { String($0) }
            .filter { $0.isBlank == false }
        
        var urlsContainsPartsOfBankName = [Int : [String]]()

        urlCandidates.forEach { urlCandidate in
            if let containingCountParts = findBankNameInUrlHost(urlCandidate: urlCandidate, bankNameParts: bankNameParts) {
                if (urlsContainsPartsOfBankName.keys.contains(containingCountParts) == false) {
                    urlsContainsPartsOfBankName[containingCountParts] = [urlCandidate]
                }
                else {
                    urlsContainsPartsOfBankName[containingCountParts]?.append(urlCandidate)
                }
            }
        }

        if let countMostMatches = urlsContainsPartsOfBankName.keys.max() {
            return urlsContainsPartsOfBankName[countMostMatches]?.first
        }

        return nil
    }

    private func findBankNameInUrlHost(urlCandidate: String, bankNameParts: [String]) -> Int? {
        if let candidateHost = URL(string: urlCandidate.replacingOccurrences(of: "onlinebanking-", with: ""))?.host {
            return bankNameParts.filter { part in candidateHost.localizedCaseInsensitiveContains(part) }.count
        }

        return nil
    }

    private func getMainPageForBankUrl(urlForBank: String?, urlCandidates: [String]) -> String? {
        if let urlForBank = urlForBank {
            if isHomePage(urlForBank) {
                return urlForBank
            }

            if let bankUri = URL(string: urlForBank) {
                if let bankUriHost = bankUri.host {
                    let candidateUrl = urlCandidates.first { candidateUrl in
                        return URL(string: candidateUrl)?.host == bankUriHost && isHomePage(candidateUrl)
                    }
                    
                    if let candidateUrl = candidateUrl {
                        return candidateUrl
                    }
                }
            }
        }

        if let urlForBank = urlForBank {
            if let bankUri = URL(string: urlForBank) {
                return "\(bankUri.scheme ?? "")://\(bankUri.host ?? "")"
            }
        }

        return nil
    }

    private func isHomePage(_ url: String) -> Bool {
        let uri = URL(string: url)

        if let uri = uri, uri.path.isBlank && uri.host?.starts(with: "www.") == true {
            return true
        }

        return false
    }

    private func isUnlikelyBankUrl(bankName: String, urlCandidate: String) -> Bool {
        return urlCandidate.contains("meinprospekt.de/")
                || urlCandidate.contains("onlinestreet.de/")
                || urlCandidate.contains("iban-blz.de/")
                || urlCandidate.contains("bankleitzahlen.ws/")
                || urlCandidate.contains("bankleitzahl-finden.de/")
                || urlCandidate.contains("bankleitzahl-bic.de/")
                || urlCandidate.contains("bankleitzahlensuche.org/")
                || urlCandidate.contains("bankleitzahlensuche.com/")
                || urlCandidate.contains("bankverzeichnis.com")
                || urlCandidate.contains("banksuche.com/")
                || urlCandidate.contains("bank-code.net/")
                || urlCandidate.contains("thebankcodes.com/")
                || urlCandidate.contains("zinsen-berechnen.de/")
                || urlCandidate.contains("kredit-anzeiger.com/")
                || urlCandidate.contains("kreditbanken.de/")
                || urlCandidate.contains("nifox.de/")
                || urlCandidate.contains("wikipedia.org/")
                || urlCandidate.contains("transferwise.com/")
                || urlCandidate.contains("wogibtes.info/")
                || urlCandidate.contains("11880.com/")
                || urlCandidate.contains("kaufda.de/")
                || urlCandidate.contains("boomle.com/")
                || urlCandidate.contains("berlin.de/")
                || urlCandidate.contains("berliner-zeitung.de")
    }
    
    
}
