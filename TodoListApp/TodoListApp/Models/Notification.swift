import Foundation

struct Notification: Codable, Identifiable {
    let id: String
    let userId: String
    let type: NotificationType
    let title: String
    let message: String
    let status: NotificationStatus
    let priority: NotificationPriority
    let createdAt: String
    let sentAt: String?
    let readAt: String?
    let expiresAt: String?
    let todoId: String?
    let actionUrl: String?
    let metadata: String?
    let deletedAt: String?
    let deleted: Bool
    let read: Bool
    let expired: Bool
    let sent: Bool
    
    // Computed properties for backward compatibility
    var isRead: Bool { read }
    var relatedEntityId: String? { todoId }
    var relatedEntityType: String? { todoId != nil ? "todo" : nil }
    
    var formattedCreatedAt: String {
        let formatter = ISO8601DateFormatter()
        guard let date = formatter.date(from: createdAt) else { return "" }
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .short
        displayFormatter.timeStyle = .short
        return displayFormatter.string(from: date)
    }
    
    var timeAgo: String {
        let formatter = ISO8601DateFormatter()
        guard let date = formatter.date(from: createdAt) else { return "" }
        
        let now = Date()
        let timeInterval = now.timeIntervalSince(date)
        
        if timeInterval < 60 {
            return "Just now"
        } else if timeInterval < 3600 {
            let minutes = Int(timeInterval / 60)
            return "\(minutes)m ago"
        } else if timeInterval < 86400 {
            let hours = Int(timeInterval / 3600)
            return "\(hours)h ago"
        } else {
            let days = Int(timeInterval / 86400)
            return "\(days)d ago"
        }
    }
}

enum NotificationStatus: String, Codable, CaseIterable {
    case pending = "PENDING"
    case sent = "SENT"
    case read = "READ"
    case failed = "FAILED"
    
    var displayName: String {
        switch self {
        case .pending: return "Pending"
        case .sent: return "Sent"
        case .read: return "Read"
        case .failed: return "Failed"
        }
    }
}

enum NotificationPriority: String, Codable, CaseIterable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
    case urgent = "URGENT"
    
    var displayName: String {
        switch self {
        case .low: return "Low"
        case .medium: return "Medium"
        case .high: return "High"
        case .urgent: return "Urgent"
        }
    }
}

enum NotificationType: String, Codable, CaseIterable {
    case todoCreated = "TODO_CREATED"
    case todoUpdated = "TODO_UPDATED"
    case todoCompleted = "TODO_COMPLETED"
    case todoDeleted = "TODO_DELETED"
    case todoOverdue = "TODO_OVERDUE"
    case todoDueSoon = "TODO_DUE_SOON"
    case welcome = "WELCOME"
    case system = "SYSTEM"
    
    var displayName: String {
        switch self {
        case .todoCreated: return "Todo Created"
        case .todoUpdated: return "Todo Updated"
        case .todoCompleted: return "Todo Completed"
        case .todoDeleted: return "Todo Deleted"
        case .todoOverdue: return "Todo Overdue"
        case .todoDueSoon: return "Todo Due Soon"
        case .welcome: return "Welcome"
        case .system: return "System"
        }
    }
    
    var icon: String {
        switch self {
        case .todoCreated: return "plus.circle"
        case .todoUpdated: return "pencil.circle"
        case .todoCompleted: return "checkmark.circle.fill"
        case .todoDeleted: return "trash.circle"
        case .todoOverdue: return "exclamationmark.triangle.fill"
        case .todoDueSoon: return "clock.fill"
        case .welcome: return "hand.wave.fill"
        case .system: return "gear"
        }
    }
    
    var color: String {
        switch self {
        case .todoCreated: return "blue"
        case .todoUpdated: return "orange"
        case .todoCompleted: return "green"
        case .todoDeleted: return "red"
        case .todoOverdue: return "red"
        case .todoDueSoon: return "yellow"
        case .welcome: return "purple"
        case .system: return "gray"
        }
    }
}

// MARK: - Sample Data
extension Notification {
    static let samples = [
        Notification(
            id: "1",
            userId: "1",
            type: .welcome,
            title: "Welcome to TodoList!",
            message: "Thanks for joining us. Start by creating your first todo.",
            status: .pending,
            priority: .medium,
            createdAt: "2024-01-01T00:00:00Z",
            sentAt: nil,
            readAt: nil,
            expiresAt: nil,
            todoId: nil,
            actionUrl: nil,
            metadata: nil,
            deletedAt: nil,
            deleted: false,
            read: false,
            expired: false,
            sent: false
        ),
        Notification(
            id: "2",
            userId: "1",
            type: .todoCreated,
            title: "New Todo Created",
            message: "You created a new todo: 'Complete iOS app design'",
            status: .pending,
            priority: .medium,
            createdAt: "2024-01-01T10:00:00Z",
            sentAt: nil,
            readAt: nil,
            expiresAt: nil,
            todoId: "1",
            actionUrl: nil,
            metadata: nil,
            deletedAt: nil,
            deleted: false,
            read: false,
            expired: false,
            sent: false
        ),
        Notification(
            id: "3",
            userId: "1",
            type: .todoOverdue,
            title: "Todo Overdue",
            message: "Your todo 'Review backend API' is overdue",
            status: .read,
            priority: .high,
            createdAt: "2024-01-01T12:00:00Z",
            sentAt: "2024-01-01T12:15:00Z",
            readAt: "2024-01-01T12:30:00Z",
            expiresAt: nil,
            todoId: "2",
            actionUrl: nil,
            metadata: nil,
            deletedAt: nil,
            deleted: false,
            read: true,
            expired: false,
            sent: true
        )
    ]
}
