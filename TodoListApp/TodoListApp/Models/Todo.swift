import Foundation
import SwiftUI

struct Todo: Codable, Identifiable {
    let id: String
    let title: String
    let description: String?
    let userId: String
    let status: TodoStatus
    let priority: TodoPriority
    let createdAt: String
    let updatedAt: String
    let dueDate: String?
    let completedAt: String?
    let tags: [String]?
    let attachments: [String]?
    let deleted: Bool
    let overdue: Bool
    let completed: Bool
    
    var isCompleted: Bool {
        return completed
    }
    
    var isOverdue: Bool {
        return overdue
    }
    
    var isDeleted: Bool {
        return deleted
    }
    
    var formattedDueDate: String? {
        guard let dueDateString = dueDate else { return nil }
        let formatter = ISO8601DateFormatter()
        guard let dueDate = formatter.date(from: dueDateString) else { return nil }
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .medium
        displayFormatter.timeStyle = .short
        return displayFormatter.string(from: dueDate)
    }
}

enum TodoStatus: String, Codable, CaseIterable {
    case pending = "PENDING"
    case inProgress = "IN_PROGRESS"
    case completed = "COMPLETED"
    case cancelled = "CANCELLED"
    
    var displayName: String {
        switch self {
        case .pending: return "Pending"
        case .inProgress: return "In Progress"
        case .completed: return "Completed"
        case .cancelled: return "Cancelled"
        }
    }
    
    var color: Color {
        switch self {
        case .pending: return .orange
        case .inProgress: return .blue
        case .completed: return .green
        case .cancelled: return .red
        }
    }
    
    var icon: String {
        switch self {
        case .pending: return "clock"
        case .inProgress: return "play.circle"
        case .completed: return "checkmark.circle.fill"
        case .cancelled: return "xmark.circle"
        }
    }
}

enum TodoPriority: String, Codable, CaseIterable {
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
    
    var color: Color {
        switch self {
        case .low: return .gray
        case .medium: return .blue
        case .high: return .orange
        case .urgent: return .red
        }
    }
    
    var icon: String {
        switch self {
        case .low: return "1.circle"
        case .medium: return "2.circle"
        case .high: return "3.circle"
        case .urgent: return "exclamationmark.triangle.fill"
        }
    }
}

// MARK: - Sample Data
extension Todo {
    static let samples = [
        Todo(
            id: "1",
            title: "Complete iOS app design",
            description: "Design the main screens and user flow for the TodoList app",
            userId: "1",
            status: .inProgress,
            priority: .high,
            createdAt: "2024-01-01T00:00:00Z",
            updatedAt: "2024-01-01T00:00:00Z",
            dueDate: "2024-01-15T18:00:00Z",
            completedAt: nil,
            tags: ["iOS", "Design"],
            attachments: nil,
            deleted: false,
            overdue: false,
            completed: false
        ),
        Todo(
            id: "2",
            title: "Review backend API",
            description: "Test all the API endpoints and ensure they work correctly",
            userId: "1",
            status: .pending,
            priority: .medium,
            createdAt: "2024-01-01T00:00:00Z",
            updatedAt: "2024-01-01T00:00:00Z",
            dueDate: nil,
            completedAt: nil,
            tags: ["Backend", "Testing"],
            attachments: nil,
            deleted: false,
            overdue: false,
            completed: false
        ),
        Todo(
            id: "3",
            title: "Setup CI/CD pipeline",
            description: "Configure automated testing and deployment",
            userId: "1",
            status: .completed,
            priority: .urgent,
            createdAt: "2024-01-01T00:00:00Z",
            updatedAt: "2024-01-01T00:00:00Z",
            dueDate: "2024-01-10T12:00:00Z",
            completedAt: "2024-01-09T15:30:00Z",
            tags: ["DevOps", "CI/CD"],
            attachments: nil,
            deleted: false,
            overdue: false,
            completed: true
        )
    ]
}
