import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var authService: AuthService
    @Environment(\.dismiss) private var dismiss
    
    @State private var username = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var firstName = ""
    @State private var lastName = ""
    @State private var showingAlert = false
    @State private var errorMessage = ""
    @State private var passwordStrength = ""
    @State private var passwordStrengthColor: Color = .gray
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Header
                    VStack(spacing: 8) {
                        Text("Create Account")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                        
                        Text("Join TodoList today")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.top)
                    
                    // Registration Form
                    VStack(spacing: 16) {
                        // Personal Information
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Personal Information")
                                .font(.headline)
                            
                            HStack(spacing: 12) {
                                TextField("First Name", text: $firstName)
                                    .textFieldStyle(RoundedBorderTextFieldStyle())
                                
                                TextField("Last Name", text: $lastName)
                                    .textFieldStyle(RoundedBorderTextFieldStyle())
                            }
                        }
                        
                        // Account Information
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Account Information")
                                .font(.headline)
                            
                            TextField("Username", text: $username)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                            
                            TextField("Email", text: $email)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                        }
                        
                        // Password Section
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Password")
                                .font(.headline)
                            
                            SecureField("Password", text: $password)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .onChange(of: password) { _ in
                                    updatePasswordStrength()
                                }
                            
                            // Password Strength Indicator
                            if !password.isEmpty {
                                HStack {
                                    Text("Strength:")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    
                                    Text(passwordStrength)
                                        .font(.caption)
                                        .fontWeight(.medium)
                                        .foregroundColor(passwordStrengthColor)
                                    
                                    Spacer()
                                }
                            }
                            
                            SecureField("Confirm Password", text: $confirmPassword)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                            
                            // Password Match Indicator
                            if !confirmPassword.isEmpty {
                                HStack {
                                    Image(systemName: password == confirmPassword ? "checkmark.circle.fill" : "xmark.circle.fill")
                                        .foregroundColor(password == confirmPassword ? .green : .red)
                                    
                                    Text(password == confirmPassword ? "Passwords match" : "Passwords don't match")
                                        .font(.caption)
                                        .foregroundColor(password == confirmPassword ? .green : .red)
                                    
                                    Spacer()
                                }
                            }
                        }
                        
                        // Register Button
                        Button(action: {
                            Task {
                                await register()
                            }
                        }) {
                            HStack {
                                if authService.isLoading {
                                    ProgressView()
                                        .scaleEffect(0.8)
                                }
                                Text("Create Account")
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(isFormValid ? Color.blue : Color.gray)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                        .disabled(!isFormValid || authService.isLoading)
                        
                        // Password Requirements
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Password Requirements:")
                                .font(.caption)
                                .fontWeight(.medium)
                            
                            Text("• At least 8 characters")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            
                            Text("• Uppercase and lowercase letters")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            
                            Text("• At least one number")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            
                            Text("• At least one special character")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding(.top)
                    }
                    .padding(.horizontal)
                    
                    Spacer()
                }
            }
            .navigationTitle("Register")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
            .alert("Error", isPresented: $showingAlert) {
                Button("OK") { }
            } message: {
                Text(errorMessage)
            }
        }
    }
    
    private var isFormValid: Bool {
        return !username.isEmpty &&
               !email.isEmpty &&
               !password.isEmpty &&
               !confirmPassword.isEmpty &&
               password == confirmPassword &&
               password.count >= 8
    }
    
    private func updatePasswordStrength() {
        let strength = getPasswordStrength(password)
        passwordStrength = strength.text
        passwordStrengthColor = strength.color
    }
    
    private func getPasswordStrength(_ password: String) -> (text: String, color: Color) {
        if password.count < 8 {
            return ("Too short", .red)
        }
        
        var score = 0
        if password.count >= 8 { score += 1 }
        if password.rangeOfCharacter(from: .uppercaseLetters) != nil { score += 1 }
        if password.rangeOfCharacter(from: .lowercaseLetters) != nil { score += 1 }
        if password.rangeOfCharacter(from: .decimalDigits) != nil { score += 1 }
        if password.rangeOfCharacter(from: CharacterSet(charactersIn: "!@#$%^&*()_+-=[]{}|;:,.<>?")) != nil { score += 1 }
        
        switch score {
        case 1: return ("Very weak", .red)
        case 2: return ("Weak", .orange)
        case 3: return ("Fair", .yellow)
        case 4: return ("Good", .blue)
        case 5: return ("Strong", .green)
        default: return ("Unknown", .gray)
        }
    }
    
    private func register() async {
        do {
            _ = try await authService.register(
                username: username,
                email: email,
                password: password,
                confirmPassword: confirmPassword,
                firstName: firstName.isEmpty ? nil : firstName,
                lastName: lastName.isEmpty ? nil : lastName
            )
            
            await MainActor.run {
                dismiss()
            }
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
                showingAlert = true
            }
        }
    }
}

#Preview {
    RegisterView()
        .environmentObject(AuthService.shared)
}
