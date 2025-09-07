import Foundation

class NotificationService: ObservableObject {
    static let shared = NotificationService()
    
    private let config = NetworkConfigManager.shared
    
    private init() {}
    
    // MARK: - Notification CRUD Operations
    func getAllNotifications() async throws -> [Notification] {
        let notifications: [Notification] = try await APIClient.shared.request(
            endpoint: config.endpoints.notifications.getAll,
            responseType: [Notification].self
        )
        return notifications
    }
    
    func getNotification(id: String) async throws -> Notification {
        let notification: Notification = try await APIClient.shared.request(
            endpoint: config.endpoints.notifications.getAll + "/\(id)",
            responseType: Notification.self
        )
        return notification
    }
    
    func markAsRead(id: String) async throws -> Notification {
        let endpoint = config.endpoints.notifications.markAsRead.replacingOccurrences(of: "{id}", with: id)
        let notification: Notification = try await APIClient.shared.request(
            endpoint: endpoint,
            method: .PUT,
            responseType: Notification.self
        )
        return notification
    }
    
    func markAllAsRead() async throws -> [String: String] {
        let response: [String: String] = try await APIClient.shared.request(
            endpoint: config.endpoints.notifications.markAllAsRead,
            method: .PUT,
            responseType: [String: String].self
        )
        return response
    }
    
    func deleteNotification(id: String) async throws {
        let endpoint = config.endpoints.notifications.delete.replacingOccurrences(of: "{id}", with: id)
        let _: [String: String] = try await APIClient.shared.request(
            endpoint: endpoint,
            method: .DELETE,
            responseType: [String: String].self
        )
    }
    
    // MARK: - Statistics
    func getUnreadCount() async throws -> Int {
        let notifications = try await getAllNotifications()
        return notifications.filter { !$0.isRead }.count
    }
    
    func getNotificationsByType(_ type: NotificationType) async throws -> [Notification] {
        let notifications = try await getAllNotifications()
        return notifications.filter { $0.type == type }
    }
}
