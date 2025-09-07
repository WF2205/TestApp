import Foundation
import Combine

class APIClient: ObservableObject {
    static let shared = APIClient()
    
    private let config = NetworkConfigManager.shared
    private let session: URLSession
    
    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = config.requestTimeout
        configuration.timeoutIntervalForResource = config.resourceTimeout
        
        // Enable cookie storage to maintain session
        configuration.httpCookieStorage = HTTPCookieStorage.shared
        configuration.httpCookieAcceptPolicy = .always
        
        // Ensure cookies are persisted across app launches
        HTTPCookieStorage.shared.cookieAcceptPolicy = .always
        
        self.session = URLSession(configuration: configuration)
        
        // Restore cookies from UserDefaults
        restoreCookiesFromUserDefaults()
        
        // Log current cookies on initialization
        print("🍪 APIClient initialized with cookies:")
        if let cookies = HTTPCookieStorage.shared.cookies {
            for cookie in cookies {
                print("   \(cookie.name)=\(cookie.value) (domain: \(cookie.domain))")
            }
        } else {
            print("   No cookies stored")
        }
    }
    
    // MARK: - Cookie Management
    private func saveCookiesToUserDefaults() {
        if let cookies = HTTPCookieStorage.shared.cookies {
            let cookieData = cookies.map { (cookie: HTTPCookie) in
                [
                    "name": cookie.name,
                    "cookieValue": cookie.value as String,
                    "domain": cookie.domain,
                    "path": cookie.path,
                    "expires": cookie.expiresDate?.timeIntervalSince1970 ?? 0
                ]
            }
            UserDefaults.standard.set(cookieData, forKey: "savedCookies")
            print("🍪 Saved \(cookies.count) cookies to UserDefaults")
        }
    }
    
    private func restoreCookiesFromUserDefaults() {
        if let cookieData = UserDefaults.standard.array(forKey: "savedCookies") as? [[String: Any]] {
            for cookieDict in cookieData {
                if let name = cookieDict["name"] as? String,
                   let cookieValue = cookieDict["cookieValue"] as? String,
                   let domain = cookieDict["domain"] as? String,
                   let path = cookieDict["path"] as? String {
                    
                    var properties: [HTTPCookiePropertyKey: Any] = [
                        HTTPCookiePropertyKey.name: name,
                        HTTPCookiePropertyKey.value: cookieValue,
                        HTTPCookiePropertyKey.domain: domain,
                        HTTPCookiePropertyKey.path: path
                    ]
                    
                    if let expires = cookieDict["expires"] as? TimeInterval, expires > 0 {
                        properties[HTTPCookiePropertyKey.expires] = Date(timeIntervalSince1970: expires)
                    }
                    
                    if let cookie = HTTPCookie(properties: properties) {
                        HTTPCookieStorage.shared.setCookie(cookie)
                    }
                }
            }
            print("🍪 Restored \(cookieData.count) cookies from UserDefaults")
        }
    }
    
    // MARK: - Generic Request Method
    func request<T: Codable>(
        endpoint: String,
        method: HTTPMethod = .GET,
        body: Data? = nil,
        responseType: T.Type,
        parameters: [String: String] = [:]
    ) async throws -> T {
        guard let url = config.getFullURL(for: endpoint, parameters: parameters) else {
            print("❌ API Request Failed: Invalid URL for endpoint: \(endpoint)")
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Log request details
        print("🚀 API Request:")
        print("   URL: \(url.absoluteString)")
        print("   Method: \(method.rawValue)")
        print("   Headers: \(request.allHTTPHeaderFields ?? [:])")
        
        // Log cookies being sent
        if let cookies = HTTPCookieStorage.shared.cookies(for: url) {
            print("   Cookies: \(cookies.map { "\($0.name)=\($0.value)" }.joined(separator: ", "))")
        } else {
            print("   Cookies: None")
        }
        
        if let body = body {
            request.httpBody = body
            if let bodyString = String(data: body, encoding: .utf8) {
                print("   Body: \(bodyString)")
            } else {
                print("   Body: [Binary data - \(body.count) bytes]")
            }
        }
        
        do {
            let (data, response) = try await session.data(for: request)
            
            // Log response details
            guard let httpResponse = response as? HTTPURLResponse else {
                print("❌ API Response Failed: Invalid response type")
                throw APIError.invalidResponse
            }
            
            print("📥 API Response:")
            print("   Status Code: \(httpResponse.statusCode)")
            print("   Headers: \(httpResponse.allHeaderFields)")
            
            // Log cookies if any
            if let cookies = HTTPCookieStorage.shared.cookies(for: url) {
                print("   Cookies: \(cookies.map { "\($0.name)=\($0.value)" }.joined(separator: "; "))")
            }
            
            if let responseString = String(data: data, encoding: .utf8) {
                print("   Body: \(responseString)")
            } else {
                print("   Body: [Binary data - \(data.count) bytes]")
            }
            
                   guard httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 else {
                       print("❌ API Response Failed: HTTP \(httpResponse.statusCode)")
                       throw APIError.httpError(httpResponse.statusCode)
                   }
                   
                   // Check for session expiration
                   if let responseString = String(data: data, encoding: .utf8),
                      responseString.contains("loginUrl") && responseString.contains("Please login") {
                       print("🔐 Session expired detected!")
                       print("   Response body: \(responseString)")
                       print("   Clearing authentication state and cookies...")
                       
                       // Clear saved cookies
                       UserDefaults.standard.removeObject(forKey: "savedCookies")
                       HTTPCookieStorage.shared.removeCookies(since: Date.distantPast)
                       
                       // Clear authentication state without calling logout endpoint
                       Task {
                           await AuthService.shared.clearLocalAuthState()
                       }
                       throw APIError.sessionExpired
                   }
            
            do {
                let decodedResponse = try JSONDecoder().decode(T.self, from: data)
                print("✅ API Decoding Successful: \(T.self)")
                
                // Save cookies after successful request
                saveCookiesToUserDefaults()
                
                return decodedResponse
            } catch {
                print("❌ API Decoding Failed:")
                print("   Expected Type: \(T.self)")
                print("   Decoding Error: \(error)")
                if let decodingError = error as? DecodingError {
                    switch decodingError {
                    case .typeMismatch(let type, let context):
                        print("   Type Mismatch: Expected \(type), Context: \(context)")
                    case .valueNotFound(let type, let context):
                        print("   Value Not Found: \(type), Context: \(context)")
                    case .keyNotFound(let key, let context):
                        print("   Key Not Found: \(key), Context: \(context)")
                    case .dataCorrupted(let context):
                        print("   Data Corrupted: \(context)")
                    @unknown default:
                        print("   Unknown Decoding Error: \(decodingError)")
                    }
                }
                throw APIError.decodingError(error)
            }
        } catch {
            print("❌ API Request Failed: \(error)")
            throw error
        }
    }
    
    // MARK: - Upload Method
    func upload<T: Codable>(
        endpoint: String,
        data: Data,
        fileName: String,
        mimeType: String,
        responseType: T.Type,
        parameters: [String: String] = [:]
    ) async throws -> T {
        guard let url = config.getFullURL(for: endpoint, parameters: parameters) else {
            print("❌ Upload Request Failed: Invalid URL for endpoint: \(endpoint)")
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var body = Data()
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        body.append(data)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        request.httpBody = body
        
        // Log upload request details
        print("📤 Upload Request:")
        print("   URL: \(url.absoluteString)")
        print("   Method: POST")
        print("   Headers: \(request.allHTTPHeaderFields ?? [:])")
        print("   File: \(fileName)")
        print("   MIME Type: \(mimeType)")
        print("   Data Size: \(data.count) bytes")
        
        do {
            let (responseData, response) = try await session.data(for: request)
            
            // Log upload response details
            guard let httpResponse = response as? HTTPURLResponse else {
                print("❌ Upload Response Failed: Invalid response type")
                throw APIError.invalidResponse
            }
            
            print("📥 Upload Response:")
            print("   Status Code: \(httpResponse.statusCode)")
            print("   Headers: \(httpResponse.allHeaderFields)")
            
            if let responseString = String(data: responseData, encoding: .utf8) {
                print("   Body: \(responseString)")
            } else {
                print("   Body: [Binary data - \(responseData.count) bytes]")
            }
            
            guard httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 else {
                print("❌ Upload Response Failed: HTTP \(httpResponse.statusCode)")
                throw APIError.httpError(httpResponse.statusCode)
            }
            
            do {
                let decodedResponse = try JSONDecoder().decode(T.self, from: responseData)
                print("✅ Upload Decoding Successful: \(T.self)")
                return decodedResponse
            } catch {
                print("❌ Upload Decoding Failed:")
                print("   Expected Type: \(T.self)")
                print("   Decoding Error: \(error)")
                if let decodingError = error as? DecodingError {
                    switch decodingError {
                    case .typeMismatch(let type, let context):
                        print("   Type Mismatch: Expected \(type), Context: \(context)")
                    case .valueNotFound(let type, let context):
                        print("   Value Not Found: \(type), Context: \(context)")
                    case .keyNotFound(let key, let context):
                        print("   Key Not Found: \(key), Context: \(context)")
                    case .dataCorrupted(let context):
                        print("   Data Corrupted: \(context)")
                    @unknown default:
                        print("   Unknown Decoding Error: \(decodingError)")
                    }
                }
                throw APIError.decodingError(error)
            }
        } catch {
            print("❌ Upload Request Failed: \(error)")
            throw error
        }
    }
}

// MARK: - HTTP Methods
enum HTTPMethod: String {
    case GET = "GET"
    case POST = "POST"
    case PUT = "PUT"
    case DELETE = "DELETE"
}

// MARK: - API Errors
enum APIError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(Int)
    case decodingError(Error)
    case networkError(Error)
    case sessionExpired
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .invalidResponse:
            return "Invalid response"
        case .httpError(let code):
            return "HTTP Error: \(code)"
        case .decodingError(let error):
            return "Decoding Error: \(error.localizedDescription)"
        case .networkError(let error):
            return "Network Error: \(error.localizedDescription)"
        case .sessionExpired:
            return "Session expired. Please login again."
        }
    }
}
