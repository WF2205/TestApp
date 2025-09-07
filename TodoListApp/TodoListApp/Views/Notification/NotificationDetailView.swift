import SwiftUI

struct NotificationDetailView: View {
    let notification: Notification
    let onMarkAsRead: () -> Void
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header Section
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Image(systemName: notification.type.icon)
                                .font(.title2)
                                .foregroundColor(colorForType(notification.type))
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(notification.title)
                                    .font(.title2)
                                    .fontWeight(.bold)
                                
                                Text(notification.type.displayName)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            if !notification.isRead {
                                Circle()
                                    .fill(Color.blue)
                                    .frame(width: 12, height: 12)
                            }
                        }
                        
                        Text(notification.message)
                            .font(.body)
                            .foregroundColor(.primary)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    // Details Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Details")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        DetailRow(title: "Status", value: notification.status.displayName)
                        DetailRow(title: "Priority", value: notification.priority.displayName)
                        DetailRow(title: "Created", value: formatDetailedDate(notification.createdAt))
                        
                        if let readAt = notification.readAt {
                            DetailRow(title: "Read At", value: formatDetailedDate(readAt))
                        }
                        
                        if let sentAt = notification.sentAt {
                            DetailRow(title: "Sent At", value: formatDetailedDate(sentAt))
                        }
                        
                        if let expiresAt = notification.expiresAt {
                            DetailRow(title: "Expires At", value: formatDetailedDate(expiresAt))
                        }
                        
                        if let todoId = notification.todoId {
                            DetailRow(title: "Related Todo", value: todoId)
                        }
                        
                        if let actionUrl = notification.actionUrl {
                            DetailRow(title: "Action URL", value: actionUrl)
                        }
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    Spacer(minLength: 20)
                }
                .padding()
            }
            .navigationTitle("Notification")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    if !notification.isRead {
                        Button("Mark Read") {
                            onMarkAsRead()
                            dismiss()
                        }
                        .foregroundColor(.blue)
                    }
                }
            }
        }
    }
    
    private func colorForType(_ type: NotificationType) -> Color {
        switch type {
        case .todoCreated: return .blue
        case .todoUpdated: return .orange
        case .todoCompleted: return .green
        case .todoDeleted: return .red
        case .todoOverdue: return .red
        case .todoDueSoon: return .yellow
        case .welcome: return .purple
        case .system: return .gray
        }
    }
    
    private func formatDetailedDate(_ dateString: String) -> String {
        let formatter = ISO8601DateFormatter()
        guard let date = formatter.date(from: dateString) else { return dateString }
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .full
        displayFormatter.timeStyle = .medium
        return displayFormatter.string(from: date)
    }
}

struct DetailRow: View {
    let title: String
    let value: String
    
    var body: some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(width: 100, alignment: .leading)
            
            Text(value)
                .font(.subheadline)
                .foregroundColor(.primary)
            
            Spacer()
        }
    }
}

#Preview {
    NotificationDetailView(
        notification: Notification.samples[0],
        onMarkAsRead: {}
    )
}
