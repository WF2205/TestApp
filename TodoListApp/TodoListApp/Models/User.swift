import Foundation
import UIKit

struct User: Codable, Identifiable {
    let id: String
    let username: String
    let email: String
    let firstName: String?
    let lastName: String?
    let githubId: String?
    let avatarUrl: String?
    let avatarDataBase64: String? // Base64 encoded image data
    let createdAt: String
    let updatedAt: String?
    let lastLoginAt: String?
    let roles: [String]
    let active: Bool
    let passwordEnabled: Bool?
    let fullName: String? // Add fullName field from backend
    
    enum CodingKeys: String, CodingKey {
        case id, username, email, firstName, lastName, githubId, avatarUrl, avatarDataBase64
        case createdAt, updatedAt, lastLoginAt, roles, active, passwordEnabled, fullName
    }
    
    var computedFullName: String {
        if let fullName = fullName, !fullName.isEmpty {
            return fullName
        }
        if let firstName = firstName, let lastName = lastName {
            return "\(firstName) \(lastName)"
        } else if let firstName = firstName {
            return firstName
        } else if let lastName = lastName {
            return lastName
        }
        return username
    }
    
    var displayName: String {
        return computedFullName.isEmpty ? username : computedFullName
    }
    
    var memberSince: String {
        return formatDate(createdAt)
    }
    
    var lastLogin: String {
        guard let lastLoginAt = lastLoginAt else {
            return "Never"
        }
        return formatDate(lastLoginAt)
    }
    
    var memberSinceShort: String {
        return formatDateShort(createdAt)
    }
    
    var lastLoginShort: String {
        guard let lastLoginAt = lastLoginAt else {
            return "Never"
        }
        return formatDateShort(lastLoginAt)
    }
    
    // Convert Base64 avatar data to UIImage
    var avatarImage: UIImage? {
        guard let avatarDataBase64 = avatarDataBase64,
              let data = Data(base64Encoded: avatarDataBase64) else {
            return nil
        }
        return UIImage(data: data)
    }
    
    private func formatDate(_ dateString: String) -> String {
        // Try ISO8601 first
        let iso8601Formatter = ISO8601DateFormatter()
        if let date = iso8601Formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .medium
            displayFormatter.timeStyle = .short
            return displayFormatter.string(from: date)
        }
        
        // Try with Z suffix added
        let dateWithZ = dateString.hasSuffix("Z") ? dateString : dateString + "Z"
        if let date = iso8601Formatter.date(from: dateWithZ) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .medium
            displayFormatter.timeStyle = .short
            return displayFormatter.string(from: date)
        }
        
        // Try custom format for backend dates
        let customFormatter = DateFormatter()
        customFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        customFormatter.timeZone = TimeZone(abbreviation: "UTC")
        if let date = customFormatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .medium
            displayFormatter.timeStyle = .short
            return displayFormatter.string(from: date)
        }
        
        return "Unknown"
    }
    
    private func formatDateShort(_ dateString: String) -> String {
        // Try ISO8601 first
        let iso8601Formatter = ISO8601DateFormatter()
        if let date = iso8601Formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .short
            displayFormatter.timeStyle = .none
            return displayFormatter.string(from: date)
        }
        
        // Try with Z suffix added
        let dateWithZ = dateString.hasSuffix("Z") ? dateString : dateString + "Z"
        if let date = iso8601Formatter.date(from: dateWithZ) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .short
            displayFormatter.timeStyle = .none
            return displayFormatter.string(from: date)
        }
        
        // Try custom format for backend dates
        let customFormatter = DateFormatter()
        customFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        customFormatter.timeZone = TimeZone(abbreviation: "UTC")
        if let date = customFormatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .short
            displayFormatter.timeStyle = .none
            return displayFormatter.string(from: date)
        }
        
        return "Unknown"
    }
}

// MARK: - Sample Data
extension User {
    static let sample = User(
        id: "1",
        username: "johndoe",
        email: "john@example.com",
        firstName: "John",
        lastName: "Doe",
        githubId: "12345",
        avatarUrl: "https://avatars.githubusercontent.com/u/12345?v=4",
        avatarDataBase64: nil,
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
        lastLoginAt: "2024-01-01T00:00:00Z",
        roles: ["USER"],
        active: true,
        passwordEnabled: true,
        fullName: "John Doe"
    )
    
}
