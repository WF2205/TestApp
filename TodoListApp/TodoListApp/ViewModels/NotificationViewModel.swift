import Foundation

@MainActor
class NotificationViewModel: ObservableObject {
    @Published var notifications: [Notification] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let notificationService = NotificationService.shared
    
    func loadNotifications() async {
        isLoading = true
        errorMessage = nil
        
        do {
            notifications = try await notificationService.getAllNotifications()
        } catch {
            errorMessage = "Failed to load notifications: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
    
    func markAsRead(_ notification: Notification) async {
        do {
            let updatedNotification = try await notificationService.markAsRead(id: notification.id)
            
            // Update the notification in our local array
            if let index = notifications.firstIndex(where: { $0.id == notification.id }) {
                notifications[index] = updatedNotification
            }
        } catch {
            errorMessage = "Failed to mark notification as read: \(error.localizedDescription)"
        }
    }
    
    func markAllAsRead() async {
        do {
            _ = try await notificationService.markAllAsRead()
            
            // Update all notifications to read status
            for index in notifications.indices {
                notifications[index] = Notification(
                    id: notifications[index].id,
                    userId: notifications[index].userId,
                    type: notifications[index].type,
                    title: notifications[index].title,
                    message: notifications[index].message,
                    status: notifications[index].status,
                    priority: notifications[index].priority,
                    createdAt: notifications[index].createdAt,
                    sentAt: notifications[index].sentAt,
                    readAt: ISO8601DateFormatter().string(from: Date()),
                    expiresAt: notifications[index].expiresAt,
                    todoId: notifications[index].todoId,
                    actionUrl: notifications[index].actionUrl,
                    metadata: notifications[index].metadata,
                    deletedAt: notifications[index].deletedAt,
                    deleted: notifications[index].deleted,
                    read: true,
                    expired: notifications[index].expired,
                    sent: notifications[index].sent
                )
            }
        } catch {
            errorMessage = "Failed to mark all notifications as read: \(error.localizedDescription)"
        }
    }
}
