import SwiftUI


class UrlUtil {

    func makeLinkAbsolute(url: String, siteUrl: String) -> String {
        var absoluteUrl = url

        if(url.starts(with: "//")) {
            if(siteUrl.starts(with: "https:")) {
                absoluteUrl = "https:" + url
            }
            else {
                absoluteUrl = "http:" + url
            }
        }
        else if(url.starts(with: "/")) {
            if let url = tryToMakeUrlAbsolute(relativeUrl: url, siteUrl: siteUrl) {
                absoluteUrl = url
            }
        }
        else if(url.starts(with: "http") == false) {
            if let url = tryToMakeUrlAbsolute(relativeUrl: url, siteUrl: siteUrl) {
                absoluteUrl = url
            }
        }

        return absoluteUrl
    }

    func tryToMakeUrlAbsolute(relativeUrl: String, siteUrl: String) -> String? {
        if let relativeUri = URL.encoded(relativeUrl) {
            // TODO: how to do this check in Swift?
//            if(relativeUri.isAbsolute && relativeUri.scheme.starts(with: "http") == false) {
//                return relativeUrl // it's an absolute uri but just doesn't start with http, e.g. mailto: for file:
//            }
        }
        
        if let uri = URL.encoded(siteUrl) {
            return uri.appendingPathComponent(relativeUrl).absoluteString // i think this always works in Swift
        }

        if let uri = URL.encoded(siteUrl) {

            let port = (uri.port ?? 0) > 0 ? ":" + String(uri.port!) : ""
            let separator = relativeUrl.starts(with: "/") ? "" : "/"

            let manuallyCreatedUriString = "\(uri.scheme)://\(uri.host)\(port)\(separator)\(relativeUrl)"
            
            return URL.encoded(manuallyCreatedUriString)?.absoluteString
        }

        return nil
    }
    
}
