import Foundation
import SwiftSoup
import fints4k // TODO: get rid of this


class FaviconFinder {
    
    private let webClient = UrlSessionWebClient() // TODO: create interface and pass from SwiftBankIconFinder
    
    
    func extractFavicons(url: String, callback: @escaping ([Favicon]) -> Void) {
        webClient.getAsync(url) { response in
            if response.successful, let html = response.body {
                callback(self.extractFavicons(url: url, html: html))
            }
            else {
                callback([])
            }
        }

        return callback([])
    }

    func extractFavicons(url: String, html: String) -> [Favicon] {
        if let document = try? SwiftSoup.parse(html) {
            return extractFavicons(document: document, url: url)
        }
        
        return []
    }

    func extractFavicons(document: Document, url: String) -> [Favicon] {
        var extractedFavicons = (try? document.head()?.select("link, meta").map { mapElementToFavicon($0, url) } as? [Favicon]) ?? []

        tryToFindDefaultFavicon(url, &extractedFavicons)

        return extractedFavicons
    }

    private func tryToFindDefaultFavicon(_ url: String, _ extractedFavicons: inout [Favicon]) {
        let urlInstance = URL(string: url)
        let defaultFaviconUrl = "\(urlInstance?.scheme ?? "https")://\(urlInstance?.host ?? "")/favicon.ico"

        if (containsIconWithUrl(extractedFavicons, defaultFaviconUrl) == false) {
            let response = webClient.head(defaultFaviconUrl)
            if (response.successful) {
                extractedFavicons.append(Favicon(url: defaultFaviconUrl, iconType: FaviconType.ShortcutIcon))
            }
        }
    }

    private func containsIconWithUrl(_ favicons: [Favicon], _ faviconUrl: String) -> Bool {
        return favicons.first(where: { favicon in favicon.url == faviconUrl } ) != nil
    }

    /**
     * Possible formats are documented here https://stackoverflow.com/questions/21991044/how-to-get-high-resolution-website-logo-favicon-for-a-given-url#answer-22007642
     * and here https://en.wikipedia.org/wiki/Favicon
     */
    private func mapElementToFavicon(_ linkOrMetaElement: Element, _ siteUrl: String) -> Favicon? {
        if (linkOrMetaElement.nodeName() == "link") {
            return mapLinkElementToFavicon(linkOrMetaElement, siteUrl)
        }
        else if (linkOrMetaElement.nodeName() == "meta") {
            return mapMetaElementToFavicon(linkOrMetaElement, siteUrl)
        }

        return nil
    }

    private func mapLinkElementToFavicon(_ linkElement: Element, _ siteUrl: String) -> Favicon? {
        if linkElement.hasAttr("rel") {
            if let faviconType = getFaviconTypeForLinkElements(linkElement) {
                let href = try? linkElement.attr("href")
                let sizes = try? linkElement.attr("sizes")
                let type = try? linkElement.attr("type")

                if href?.starts(with: "data:;base64") == false {
                    return createFavicon(url: href, siteUrl: siteUrl, iconType: faviconType, sizesString: sizes, type: type)
                }
            }
        }

        return nil
    }

    private func getFaviconTypeForLinkElements(_ linkElement: Element) -> FaviconType? {
        if let relValue = try? linkElement.attr("rel") {
            switch relValue {
            case "icon":
                return FaviconType.Icon
            case "apple-touch-icon-precomposed":
                return FaviconType.AppleTouchPrecomposed
            case "apple-touch-icon":
                return FaviconType.AppleTouch
            case "shortcut icon":
                return FaviconType.ShortcutIcon
            default:
                return nil
            }
        }
        
        return nil
    }

    private func mapMetaElementToFavicon(_ metaElement: Element, _ siteUrl: String) -> Favicon? {
        if let content = try? metaElement.attr("content") {
            if isOpenGraphImageDeclaration(metaElement) {
                return Favicon(url: makeLinkAbsolute(url: content, siteUrl: siteUrl), iconType: FaviconType.OpenGraphImage)
            }
            else if isMsTileMetaElement(metaElement) {
                return Favicon(url: makeLinkAbsolute(url: content, siteUrl: siteUrl), iconType: FaviconType.MsTileImage)
            }
        }

        return nil
    }

    private func isOpenGraphImageDeclaration(_ metaElement: Element) -> Bool {
        return metaElement.hasAttr("property")
            && "og:image" == (try? metaElement.attr("property"))
            && metaElement.hasAttr("content")
    }

    private func isMsTileMetaElement(_ metaElement: Element) -> Bool {
        return metaElement.hasAttr("name")
            && "msapplication-TileImage" == (try? metaElement.attr("name"))
            && metaElement.hasAttr("content")
    }


    private func createFavicon(url: String?, siteUrl: String, iconType: FaviconType, sizesString: String?, type: String?) -> Favicon? {
        if let url = url {
            let absoluteUrl = makeLinkAbsolute(url: url, siteUrl: siteUrl)
            let size = extractSizesFromString(sizesString)
            
            return Favicon(url: absoluteUrl, iconType: iconType, size: size, type: type)
        }

        return nil
    }
    
    private func makeLinkAbsolute(url: String, siteUrl: String) -> String {
        return url // TODO
    }

    private func extractSizesFromString(_ sizesString: String?) -> Size? {
        if let sizesString = sizesString {
            let sizes = extractSizesFromString(sizesString)

            if sizes.isEmpty == false {
                return sizes.max(by: { lhs, rhs in lhs >= rhs } )
            }
        }
        
        return nil
    }

    private func extractSizesFromString(_ sizesString: String) -> [Size] {
        let sizes = sizesString.split(separator: " ").map { mapSizeString(String($0)) } as? [Size] ?? []

        return sizes
    }

    private func mapSizeString(_ sizeString: String) -> Size? {
        var parts = sizeString.split(separator: "x")
        if parts.count != 2 {
            parts = sizeString.split(separator: "×") // actually doesn't meet specification, see https://www.w3schools.com/tags/att_link_sizes.asp, but New York Times uses it
        }
        if parts.count != 2 {
            parts = sizeString.split(separator: "X")
        }

        if parts.count == 2 {
            let width = Int(parts[0])
            let height = Int(parts[1])

            if let width = width, let height = height {
                return Size(width: width, height: height)
            }
        }

        return nil
    }
    
}
