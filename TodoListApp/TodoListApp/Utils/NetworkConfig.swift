import Foundation

struct NetworkConfig: Codable {
    let baseURL: String
    let timeouts: Timeouts
    let oauth: OAuth
    let endpoints: Endpoints
    let environment: [String: Environment]
    
    struct Timeouts: Codable {
        let request: Int
        let resource: Int
    }
    
    struct OAuth: Codable {
        let github: GitHub
        
        struct GitHub: Codable {
            let authorizationURL: String
            let redirectScheme: String
            let redirectURI: String
        }
    }
    
    struct Endpoints: Codable {
        let auth: Auth
        let users: Users
        let todos: Todos
        let notifications: Notifications
        
        struct Auth: Codable {
            let login: String
            let register: String
            let logout: String
            let enablePassword: String
            let changePassword: String
            let checkPasswordStrength: String
        }
        
        struct Users: Codable {
            let profile: String
            let updateProfile: String
            let uploadAvatar: String
            let deleteAvatar: String
        }
        
        struct Todos: Codable {
            let getAll: String
            let getById: String
            let create: String
            let update: String
            let delete: String
            let complete: String
            let pending: String
            let inProgress: String
            let byStatus: String
            let byPriority: String
            let overdue: String
            let dueSoon: String
            let stats: String
        }
        
        struct Notifications: Codable {
            let getAll: String
            let markAsRead: String
            let markAllAsRead: String
            let delete: String
        }
    }
    
    struct Environment: Codable {
        let baseURL: String
        let oauth: OAuth
    }
}

class NetworkConfigManager: ObservableObject {
    static let shared = NetworkConfigManager()
    
    private(set) var config: NetworkConfig
    private let currentEnvironment: String
    
    private init() {
        // Determine current environment
        #if DEBUG
        self.currentEnvironment = "development"
        #elseif STAGING
        self.currentEnvironment = "staging"
        #else
        self.currentEnvironment = "production"
        #endif
        
        // Load configuration
        guard let config = Self.loadConfig() else {
            fatalError("Failed to load network configuration")
        }
        
        self.config = config
    }
    
    private static func loadConfig() -> NetworkConfig? {
        // First try to load from the bundle
        if let bundleURL = Bundle.main.url(forResource: "NetworkConfig", withExtension: "bundle"),
           let bundle = Bundle(url: bundleURL),
           let url = bundle.url(forResource: "NetworkConfig", withExtension: "json"),
           let data = try? Data(contentsOf: url) {
            
            do {
                let config = try JSONDecoder().decode(NetworkConfig.self, from: data)
                print("✅ Loaded NetworkConfig from bundle")
                return config
            } catch {
                print("❌ Failed to decode NetworkConfig from bundle: \(error)")
            }
        }
        
        // Fallback: try to load from main bundle directly
        if let url = Bundle.main.url(forResource: "NetworkConfig", withExtension: "json"),
           let data = try? Data(contentsOf: url) {
            
            do {
                let config = try JSONDecoder().decode(NetworkConfig.self, from: data)
                print("✅ Loaded NetworkConfig from main bundle")
                return config
            } catch {
                print("❌ Failed to decode NetworkConfig from main bundle: \(error)")
            }
        }
        
        print("❌ Failed to load NetworkConfig.json from any location")
        return nil
    }
    
    // MARK: - Environment-specific Configuration
    
    func getCurrentEnvironmentConfig() -> NetworkConfig.Environment? {
        return config.environment[currentEnvironment]
    }
    
    func getBaseURL() -> String {
        if let envConfig = getCurrentEnvironmentConfig() {
            return envConfig.baseURL
        }
        return config.baseURL
    }
    
    func getOAuthURL() -> String {
        if let envConfig = getCurrentEnvironmentConfig() {
            return envConfig.oauth.github.authorizationURL
        }
        return config.oauth.github.authorizationURL
    }
    
    // MARK: - Endpoint Helpers
    
    func getFullURL(for endpoint: String, parameters: [String: String] = [:]) -> URL? {
        let baseURL = getBaseURL()
        var fullEndpoint = endpoint
        
        // Replace parameters in endpoint
        for (key, value) in parameters {
            fullEndpoint = fullEndpoint.replacingOccurrences(of: "{\(key)}", with: value)
        }
        
        return URL(string: "\(baseURL)\(fullEndpoint)")
    }
    
    func getEndpoint(_ endpoint: String, parameters: [String: String] = [:]) -> String {
        var fullEndpoint = endpoint
        
        // Replace parameters in endpoint
        for (key, value) in parameters {
            fullEndpoint = fullEndpoint.replacingOccurrences(of: "{\(key)}", with: value)
        }
        
        return fullEndpoint
    }
    
    // MARK: - Convenience Methods
    
    var requestTimeout: TimeInterval {
        return TimeInterval(config.timeouts.request)
    }
    
    var resourceTimeout: TimeInterval {
        return TimeInterval(config.timeouts.resource)
    }
    
    var oauthRedirectScheme: String {
        return config.oauth.github.redirectScheme
    }
    
    // MARK: - Endpoints Access
    
    var endpoints: NetworkConfig.Endpoints {
        return config.endpoints
    }
}

// MARK: - Environment Detection
extension NetworkConfigManager {
    var isDevelopment: Bool {
        return currentEnvironment == "development"
    }
    
    var isStaging: Bool {
        return currentEnvironment == "staging"
    }
    
    var isProduction: Bool {
        return currentEnvironment == "production"
    }
    
    var environmentName: String {
        return currentEnvironment.capitalized
    }
}
