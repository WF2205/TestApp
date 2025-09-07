import SwiftUI

struct NotificationListView: View {
    @StateObject private var viewModel = NotificationViewModel()
    
    var body: some View {
        NavigationView {
            VStack {
                if viewModel.isLoading {
                    Spacer()
                    ProgressView("Loading notifications...")
                    Spacer()
                } else if viewModel.notifications.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "bell")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        
                        Text("No notifications")
                            .font(.title2)
                            .fontWeight(.medium)
                        
                        Text("You'll receive notifications when todos are created, updated, or completed")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    Spacer()
                } else {
                    List {
                        ForEach(viewModel.notifications) { notification in
                            NotificationRowView(notification: notification) {
                                Task {
                                    await viewModel.markAsRead(notification)
                                }
                            }
                        }
                    }
                    .refreshable {
                        await viewModel.loadNotifications()
                    }
                }
            }
            .navigationTitle("Notifications")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Mark All Read") {
                        Task {
                            await viewModel.markAllAsRead()
                        }
                    }
                    .disabled(viewModel.notifications.allSatisfy { $0.isRead })
                }
            }
        }
        .task {
            await viewModel.loadNotifications()
        }
    }
}

#Preview {
    NotificationListView()
}
