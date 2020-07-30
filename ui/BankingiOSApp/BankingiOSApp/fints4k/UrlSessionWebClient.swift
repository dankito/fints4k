import Foundation
import BankingUiSwift
import MultiplatformUtils


class UrlSessionWebClient : Fints4kIWebClient {
    
    func post(url: String, body: String, contentType: String, userAgent: String, callback: @escaping (Fints4kWebClientResponse) -> Void) {
        let request = requestFor(url, "POST", body)
        
        executeRequestAsync(request, callback)
    }
    
    func getAsync(_ url: String, callback: @escaping (Fints4kWebClientResponse) -> Void) {
        let request = requestFor(url, "GET")
        
        executeRequestAsync(request, callback)
    }
    
    
    func get(_ url: String) -> Fints4kWebClientResponse {
        let request = requestFor(url, "GET")
        
        return executeRequestSynchronous(request)
    }
    
    func getData(_ url: String) -> Data? {
        let request = requestFor(url, "GET")
        
        return executeDataRequestSynchronous(request)
    }
    
    func head(_ url: String) -> Fints4kWebClientResponse {
        let request = requestFor(url, "HEAD")
        
        return executeRequestSynchronous(request)
    }
    
    
    func requestFor(_ url: String, _ method: String, _ body: String? = nil) -> URLRequest {
        guard let requestUrl = URL.encoded(url) else { fatalError() }
        
        var request = URLRequest(url: requestUrl)
        request.httpMethod = method
        
        if let body = body {
            request.httpBody = body.data(using: String.Encoding.utf8)
        }
        
        return request
    }
    
    func executeRequestAsync(_ request: URLRequest, _ callback: @escaping (Fints4kWebClientResponse) -> Void) {
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
    
    func executeRequestSynchronous(_ request: URLRequest) -> Fints4kWebClientResponse {
        var data: Data?, urlResponse: URLResponse?, error: Error?

        let semaphore = DispatchSemaphore(value: 0)

        let dataTask = URLSession.shared.dataTask(with: request) {
            data = $0
            urlResponse = $1
            error = $2

            semaphore.signal()
        }
        
        dataTask.resume()

        _ = semaphore.wait(timeout: .distantFuture)
        
        return mapResponse(data, urlResponse, error)
    }
    
    private func mapResponse(_ data: Data?, _ response: URLResponse?, _ error: Error?) -> Fints4kWebClientResponse {
        if let httpResponse = response as? HTTPURLResponse {
            if let data = data {
                return Fints4kWebClientResponse(successful: true, responseCode: Int32(httpResponse.statusCode), error: nil, body: String(data: data, encoding: .ascii))
            }
            else {
                return Fints4kWebClientResponse(successful: true, responseCode: Int32(httpResponse.statusCode), error: nil, body: nil)
            }
        }
        else {
            return Fints4kWebClientResponse(successful: false, responseCode: -1, error: KotlinException(message: error?.localizedDescription), body: nil)
        }
    }
    
    
    func executeDataRequestSynchronous(_ request: URLRequest) -> Data? {
        var data: Data?

        let semaphore = DispatchSemaphore(value: 0)

        let dataTask = URLSession.shared.dataTask(with: request) { receivedData, _, _ in
            data = receivedData

            semaphore.signal()
        }
        
        dataTask.resume()

        _ = semaphore.wait(timeout: .distantFuture)
        
        return data
    }
    
}
