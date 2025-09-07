import Foundation

struct AuthResponse: Codable {
    let success: Bool?
    let message: String?
    let user: User?
    let error: String?
    let errors: [String: String]?
    let timestamp: String?
    let status: Int?
    
    enum CodingKeys: String, CodingKey {
        case success, message, user, error, errors, timestamp, status
    }
    
    // Computed properties for easier access
    var isSuccess: Bool {
        return success == true
    }
    
    var errorMessage: String {
        if let message = message {
            return message
        }
        if let error = error {
            return error
        }
        return "Unknown error occurred"
    }
}

struct PasswordStrengthResponse: Codable {
    let isValid: Bool
    let strength: String
}
