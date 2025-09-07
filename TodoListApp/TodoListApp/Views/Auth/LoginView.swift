import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authService: AuthService
    @State private var showingAlert = false
    @State private var showingLoginForm = false
    @State private var showingRegisterForm = false
    
    var body: some View {
        VStack(spacing: 40) {
            Spacer()
            
            // App Header
            VStack(spacing: 20) {
                Image(systemName: "checklist")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)
                
                VStack(spacing: 8) {
                    Text("TodoList")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                    
                    Text("Stay organized and productive")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
            
            // Login Options
            VStack(spacing: 20) {
                if authService.isLoading {
                    ProgressView("Checking authentication...")
                        .frame(maxWidth: .infinity)
                } else {
                    // GitHub OAuth Button
                    Button(action: {
                        authService.login()
                    }) {
                        HStack {
                            Image(systemName: "person.circle.fill")
                            Text("Login with GitHub")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(authService.isLoading)
                    
                    // Divider
                    HStack {
                        Rectangle()
                            .frame(height: 1)
                            .foregroundColor(.secondary)
                        
                        Text("or")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding(.horizontal)
                        
                        Rectangle()
                            .frame(height: 1)
                            .foregroundColor(.secondary)
                    }
                    
                    // Username/Password Login Button
                    Button(action: {
                        showingLoginForm = true
                    }) {
                        HStack {
                            Image(systemName: "person.fill")
                            Text("Login with Username/Password")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(authService.isLoading)
                    
                    // Register Button
                    Button(action: {
                        showingRegisterForm = true
                    }) {
                        Text("Don't have an account? Sign up")
                            .font(.subheadline)
                            .foregroundColor(.blue)
                    }
                }
            }
            .padding(.horizontal)
            
            Spacer()
        }
        .padding()
        .alert("Error", isPresented: $showingAlert) {
            Button("OK") { }
        } message: {
            Text(authService.errorMessage ?? "An unknown error occurred")
        }
        .onChange(of: authService.errorMessage) { errorMessage in
            if errorMessage != nil {
                showingAlert = true
            }
        }
        .sheet(isPresented: $showingLoginForm) {
            LoginFormView()
        }
        .sheet(isPresented: $showingRegisterForm) {
            RegisterView()
        }
    }
}

#Preview {
    LoginView()
        .environmentObject(AuthService.shared)
}
