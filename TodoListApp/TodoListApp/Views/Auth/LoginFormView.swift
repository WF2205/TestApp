import SwiftUI

struct LoginFormView: View {
    @EnvironmentObject var authService: AuthService
    @State private var usernameOrEmail = ""
    @State private var password = ""
    @State private var showingAlert = false
    @State private var errorMessage = ""
    
    var body: some View {
        VStack(spacing: 20) {
            // Header
            VStack(spacing: 8) {
                Text("Welcome Back")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text("Sign in to your account")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.top)
            
            // Login Form
            VStack(spacing: 16) {
                // Username/Email Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Username or Email")
                        .font(.headline)
                    
                    TextField("Enter username or email", text: $usernameOrEmail)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
                
                // Password Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Password")
                        .font(.headline)
                    
                    SecureField("Enter password", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                }
                
                // Login Button
                Button(action: {
                    Task {
                        await login()
                    }
                }) {
                    HStack {
                        if authService.isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                        }
                        Text("Sign In")
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(usernameOrEmail.isEmpty || password.isEmpty || authService.isLoading)
            }
            .padding(.horizontal)
            
            Spacer()
        }
        .alert("Error", isPresented: $showingAlert) {
            Button("OK") { }
        } message: {
            Text(errorMessage)
        }
    }
    
    private func login() async {
        do {
            _ = try await authService.loginWithPassword(
                usernameOrEmail: usernameOrEmail,
                password: password
            )
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
                showingAlert = true
            }
        }
    }
}

#Preview {
    LoginFormView()
        .environmentObject(AuthService.shared)
}
