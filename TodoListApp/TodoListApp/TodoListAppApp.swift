//
//  TodoListAppApp.swift
//  TodoListApp
//
//  Created by Wentao Fan on 9/5/25.
//

import SwiftUI

@main
struct TodoListiOSApp: App {
    @StateObject private var authService = AuthService.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(authService)
                .onOpenURL { url in
                    print("🔗 App: Received URL: \(url)")
                    // Handle OAuth callback
                    if url.scheme == "todolistios" {
                        authService.handleOAuthCallback(url: url)
                    }
                }
        }
    }
}
