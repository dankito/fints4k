import Foundation
import BankingUiSwift
import MultiplatformUtils


class UrlSessionWebClient : Fints4kIWebClient {
    
    func post(url: String, body: String, contentType: String, userAgent: String, callback: @escaping (Fints4kWebClientResponse) -> Void) {
        guard let requestUrl = URL(string: url) else { fatalError() }
        
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "POST"
        request.httpBody = body.data(using: String.Encoding.utf8)
        
        let dataTask = URLSession.shared.dataTask(with: request) { (data: Data?, response: URLResponse?, error: Error?) in
            // we have to dispatch response back to main thread as in Kotlin/Native objects can only be changed on one thread -> do all logic on main thread only network access in backbackground thread
            DispatchQueue.main.async {
                var webClientResponse = Fints4kWebClientResponse(successful: false, responseCode: -1, error: KotlinException(message: error?.localizedDescription), body: nil)
            
                if let data = data, let httpResponse = response as? HTTPURLResponse {
                    webClientResponse = Fints4kWebClientResponse(successful: true, responseCode: Int32(httpResponse.statusCode), error: nil, body: String(data: data, encoding: .ascii))
                }
                    
                callback(webClientResponse)
            }
        }

        dataTask.resume()
    }
    
}