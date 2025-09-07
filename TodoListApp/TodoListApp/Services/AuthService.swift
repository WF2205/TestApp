import Foundation
import SwiftUI
import AuthenticationServices

@MainActor
class AuthService: ObservableObject {
    static let shared = AuthService()
    
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let config = NetworkConfigManager.shared
    
    private init() {
        loadAuthState()
        // Only validate with server if we have a stored session
        if isAuthenticated {
            Task {
                await validateSessionWithServer()
            }
        }
    }
    
    // MARK: - Authentication Methods
    func checkAuthStatus() async {
        isLoading = true
        errorMessage = nil
        
        do {
            let user: User = try await APIClient.shared.request(
                endpoint: config.endpoints.users.profile,
                responseType: User.self
            )
            
            self.currentUser = user
            self.isAuthenticated = true
            saveAuthState()
        } catch {
            self.isAuthenticated = false
            self.currentUser = nil
            clearAuthState()
            print("Auth check failed: \(error)")
        }
        
        self.isLoading = false
    }
    
    private func validateSessionWithServer() async {
        // This method validates the session with the server but doesn't clear local state on failure
        // It's used on app launch to refresh user data if the session is still valid
        do {
            let user: User = try await APIClient.shared.request(
                endpoint: config.endpoints.users.profile,
                responseType: User.self
            )
            
            // Session is valid, update user data
            self.currentUser = user
            self.isAuthenticated = true
            saveAuthState()
            print("✅ Session validated successfully")
        } catch {
            // Session is invalid, but don't clear local state immediately
            // Let the user try to use the app, and if they get 401 errors, then clear state
            print("⚠️ Session validation failed: \(error)")
            // Don't clear local state here - let individual API calls handle auth failures
        }
    }
    
    func login() {
        // Open Safari for OAuth flow
        if let url = URL(string: config.getOAuthURL()) {
            UIApplication.shared.open(url)
        }
    }
    
    // MARK: - OAuth Flow Completion
    private func completeOAuthFlow() async {
        print("🔄 AuthService: Completing OAuth flow...")
        
        do {
            let response: AuthResponse = try await APIClient.shared.request(
                endpoint: "/auth/oauth/complete",
                method: .POST,
                body: nil,
                responseType: AuthResponse.self
            )
            
            if response.success == true {
                print("✅ AuthService: OAuth flow completed successfully")
                self.isAuthenticated = true
                self.currentUser = response.user
                self.errorMessage = nil
                
                // Save authentication state
                saveAuthState()
                
                print("🎉 AuthService: User logged in via OAuth: \(response.user?.username ?? "Unknown")")
            } else {
                print("❌ AuthService: OAuth completion failed: \(response.message ?? "Unknown error")")
                self.errorMessage = response.message ?? "OAuth completion failed"
            }
        } catch {
            print("❌ AuthService: OAuth completion error: \(error)")
            self.errorMessage = "Failed to complete OAuth: \(error.localizedDescription)"
        }
    }
    
    // MARK: - OAuth Callback Handling
    func handleOAuthCallback(url: URL) {
        print("🔐 AuthService: Handling OAuth callback: \(url)")
        
        // Handle success redirect
        if url.path.contains("/success") {
            print("✅ AuthService: OAuth success callback")
            Task {
                await completeOAuthFlow()
            }
            return
        }
        
        // Handle failure redirect
        if url.path.contains("/failure") {
            print("❌ AuthService: OAuth failure callback")
            errorMessage = "OAuth login failed"
            return
        }
        
        // Extract authorization code from URL
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let queryItems = components.queryItems else {
            print("❌ AuthService: Invalid callback URL")
            return
        }
        
        // Look for authorization code
        if let code = queryItems.first(where: { $0.name == "code" })?.value {
            print("✅ AuthService: Found authorization code")
            Task {
                await exchangeCodeForToken(code: code)
            }
        } else if let error = queryItems.first(where: { $0.name == "error" })?.value {
            print("❌ AuthService: OAuth error: \(error)")
            errorMessage = "OAuth error: \(error)"
        } else {
            print("❌ AuthService: No authorization code found in callback")
            errorMessage = "No authorization code received"
        }
    }
    
    private func exchangeCodeForToken(code: String) async {
        print("🔐 AuthService: Exchanging code for token")
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        do {
            // Create the token exchange request
            let parameters: [String: String] = [
                "code": code,
                "grant_type": "authorization_code",
                "client_id": "Ov23li0Ktpp1f9YEvsG6", // Your GitHub client ID
                "redirect_uri": "todolistios://oauth/callback"
            ]
            
            // Make request to your backend's token exchange endpoint
            let response: AuthResponse = try await APIClient.shared.request(
                endpoint: "/auth/oauth/token",
                method: .POST,
                responseType: AuthResponse.self,
                parameters: parameters
            )
            
            if response.isSuccess, let user = response.user {
                self.currentUser = user
                self.isAuthenticated = true
                saveAuthState()
                print("✅ AuthService: OAuth login successful")
            } else {
                self.isAuthenticated = false
                self.currentUser = nil
                clearAuthState()
                errorMessage = response.errorMessage ?? "OAuth login failed"
                print("❌ AuthService: OAuth login failed: \(response.errorMessage ?? "Unknown error")")
            }
        } catch {
            self.isAuthenticated = false
            self.currentUser = nil
            clearAuthState()
            errorMessage = "OAuth login failed: \(error.localizedDescription)"
            print("❌ AuthService: OAuth token exchange failed: \(error)")
        }
    }
    
    func loginWithPassword(usernameOrEmail: String, password: String) async throws -> User {
        print("🔐 AuthService: Starting password login for \(usernameOrEmail)")
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        let parameters: [String: String] = [
            "usernameOrEmail": usernameOrEmail,
            "password": password
        ]
        
        let jsonData = try JSONSerialization.data(withJSONObject: parameters)
        
        let response: AuthResponse = try await APIClient.shared.request(
            endpoint: config.endpoints.auth.login,
            method: .POST,
            body: jsonData,
            responseType: AuthResponse.self
        )
        
        if response.isSuccess, let user = response.user {
            self.currentUser = user
            self.isAuthenticated = true
            saveAuthState()
            return user
        } else {
            throw NSError(domain: "AuthError", code: 401, userInfo: [NSLocalizedDescriptionKey: response.errorMessage])
        }
    }
    
    func register(username: String, email: String, password: String, confirmPassword: String, firstName: String?, lastName: String?) async throws -> User {
        print("📝 AuthService: Starting registration for \(username) (\(email))")
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        var parameters: [String: Any] = [
            "username": username,
            "email": email,
            "password": password,
            "confirmPassword": confirmPassword
        ]
        
        if let firstName = firstName {
            parameters["firstName"] = firstName
        }
        if let lastName = lastName {
            parameters["lastName"] = lastName
        }
        
        let jsonData = try JSONSerialization.data(withJSONObject: parameters)
        
        do {
            let response: AuthResponse = try await APIClient.shared.request(
                endpoint: config.endpoints.auth.register,
                method: .POST,
                body: jsonData,
                responseType: AuthResponse.self
            )
            
            if response.isSuccess, let user = response.user {
                self.currentUser = user
                self.isAuthenticated = true
                saveAuthState()
                return user
            } else {
                throw NSError(domain: "AuthError", code: 400, userInfo: [NSLocalizedDescriptionKey: response.errorMessage])
            }
        } catch {
            print("❌ Registration error: \(error)")
            if let apiError = error as? APIError {
                switch apiError {
                case .decodingError(let decodingError):
                    print("❌ Decoding error details: \(decodingError)")
                default:
                    print("❌ API error: \(apiError)")
                }
            }
            throw error
        }
    }
    
    func logout() async {
        isLoading = true
        errorMessage = nil
        
        do {
            let _: [String: String] = try await APIClient.shared.request(
                endpoint: config.endpoints.auth.logout,
                method: .POST,
                responseType: [String: String].self
            )
            
            self.isAuthenticated = false
            self.currentUser = nil
            clearAuthState()
        } catch {
            self.errorMessage = "Logout failed: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
    
    func updateProfile(firstName: String, lastName: String, email: String) async throws -> User {
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        let parameters: [String: String] = [
            "firstName": firstName,
            "lastName": lastName,
            "email": email
        ]
        
        let jsonData = try JSONSerialization.data(withJSONObject: parameters)
        
        let user: User = try await APIClient.shared.request(
            endpoint: config.endpoints.users.updateProfile,
            method: .PUT,
            body: jsonData,
            responseType: User.self
        )
        
        self.currentUser = user
        saveAuthState()
        return user
    }
    
    func uploadAvatar(imageData: Data) async throws -> [String: String] {
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        let response: [String: String] = try await APIClient.shared.upload(
            endpoint: config.endpoints.users.uploadAvatar,
            data: imageData,
            fileName: "avatar.jpg",
            mimeType: "image/jpeg",
            responseType: [String: String].self
        )
        
        // Refresh user data to get updated avatar URL
        await checkAuthStatus()
        
        return response
    }
    
    func deleteAvatar() async throws {
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        let _: [String: String] = try await APIClient.shared.request(
            endpoint: config.endpoints.users.deleteAvatar,
            method: .DELETE,
            responseType: [String: String].self
        )
        
        // Refresh user data
        await checkAuthStatus()
    }
    
    // MARK: - Session Persistence
    private func saveAuthState() {
        UserDefaults.standard.set(isAuthenticated, forKey: "isAuthenticated")
        if let user = currentUser {
            do {
                let userData = try JSONEncoder().encode(user)
                UserDefaults.standard.set(userData, forKey: "currentUser")
                print("✅ Saved user to storage: \(user.username)")
            } catch {
                print("❌ Failed to encode user data: \(error)")
            }
        } else {
            UserDefaults.standard.removeObject(forKey: "currentUser")
            print("🗑️ Removed user from storage")
        }
    }
    
    private func loadAuthState() {
        isAuthenticated = UserDefaults.standard.bool(forKey: "isAuthenticated")
        if let userData = UserDefaults.standard.data(forKey: "currentUser") {
            do {
                let user = try JSONDecoder().decode(User.self, from: userData)
                currentUser = user
                print("✅ Loaded user from storage: \(user.username)")
            } catch {
                print("⚠️ Failed to decode stored user data: \(error)")
                print("   Clearing stored authentication state...")
                clearAuthState()
                isAuthenticated = false
                currentUser = nil
            }
        }
    }
    
    private func clearAuthState() {
        UserDefaults.standard.removeObject(forKey: "isAuthenticated")
        UserDefaults.standard.removeObject(forKey: "currentUser")
    }
    
    func clearLocalAuthState() async {
        // This method clears local authentication state without calling the logout endpoint
        // It's used when the server session expires
        self.isAuthenticated = false
        self.currentUser = nil
        clearAuthState()
        print("🔐 Local authentication state cleared due to session expiration")
    }
}
