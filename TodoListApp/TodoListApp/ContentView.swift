import SwiftUI

struct ContentView: View {
    @EnvironmentObject var authService: AuthService
    
    var body: some View {
        Group {
            if authService.isAuthenticated {
                MainTabView()
            } else {
                LoginView()
            }
        }
        .onAppear {
            Task {
                await authService.checkAuthStatus()
            }
        }
    }
}

struct MainTabView: View {
    @EnvironmentObject var authService: AuthService
    
    var body: some View {
        TabView {
            TodoListView()
                .tabItem {
                    Image(systemName: "checklist")
                    Text("Todos")
                }
            
            NotificationListView()
                .tabItem {
                    Image(systemName: "bell")
                    Text("Notifications")
                }
            
            ProfileView()
                .tabItem {
                    Image(systemName: "person")
                    Text("Profile")
                }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(AuthService.shared)
}
