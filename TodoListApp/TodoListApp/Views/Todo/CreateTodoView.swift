import SwiftUI

struct CreateTodoView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var description = ""
    @State private var selectedPriority = TodoPriority.medium
    @State private var dueDate = Date()
    @State private var hasDueDate = false
    @State private var isLoading = false
    @State private var showingAlert = false
    @State private var errorMessage: String?
    
    let onTodoCreated: () -> Void
    
    var body: some View {
        NavigationView {
            Form {
                Section("Todo Details") {
                    TextField("Title", text: $title)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    
                    TextField("Description (optional)", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                }
                
                Section("Priority") {
                    Picker("Priority", selection: $selectedPriority) {
                        ForEach(TodoPriority.allCases, id: \.self) { priority in
                            HStack {
                                Image(systemName: priority.icon)
                                Text(priority.displayName)
                            }
                            .tag(priority)
                        }
                    }
                    .pickerStyle(.menu)
                }
                
                Section("Due Date") {
                    Toggle("Set due date", isOn: $hasDueDate)
                    
                    if hasDueDate {
                        DatePicker(
                            "Due date",
                            selection: $dueDate,
                            displayedComponents: [.date, .hourAndMinute]
                        )
                        .datePickerStyle(.compact)
                    }
                }
                
                Section {
                    Button(action: {
                        Task {
                            await createTodo()
                        }
                    }) {
                        HStack {
                            if isLoading {
                                ProgressView()
                                    .scaleEffect(0.8)
                            }
                            Text("Create Todo")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .disabled(title.isEmpty || isLoading)
                    .buttonStyle(.borderedProminent)
                }
            }
            .navigationTitle("New Todo")
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
                Text(errorMessage ?? "An unknown error occurred")
            }
        }
    }
    
    private func createTodo() async {
        isLoading = true
        errorMessage = nil
        
        do {
            let _ = try await TodoService.shared.createTodo(
                title: title,
                description: description.isEmpty ? nil : description,
                priority: selectedPriority,
                dueDate: hasDueDate ? dueDate : nil
            )
            
            await MainActor.run {
                onTodoCreated()
                dismiss()
            }
        } catch {
            await MainActor.run {
                errorMessage = "Failed to create todo: \(error.localizedDescription)"
                showingAlert = true
            }
        }
        
        isLoading = false
    }
}

#Preview {
    CreateTodoView {
        print("Todo created")
    }
}
