import Foundation
import SwiftUI

@MainActor
class TodoListViewModel: ObservableObject {
    @Published var todos: [Todo] = []
    @Published var filteredTodos: [Todo] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showingAlert = false
    
    private let todoService = TodoService.shared
    
    func loadTodos() async {
        isLoading = true
        errorMessage = nil
        
        do {
            todos = try await todoService.getAllTodos()
            filteredTodos = todos
        } catch {
            errorMessage = "Failed to load todos: \(error.localizedDescription)"
            showingAlert = true
        }
        
        isLoading = false
    }
    
    func applyFilter(_ filter: TodoFilter) async {
        switch filter {
        case .all:
            filteredTodos = todos
        case .pending:
            filteredTodos = todos.filter { $0.status == .pending }
        case .inProgress:
            filteredTodos = todos.filter { $0.status == .inProgress }
        case .completed:
            filteredTodos = todos.filter { $0.status == .completed }
        case .overdue:
            filteredTodos = todos.filter { $0.isOverdue }
        }
    }
    
    func toggleTodoCompletion(_ todo: Todo) async {
        do {
            let updatedTodo: Todo
            
            if todo.isCompleted {
                updatedTodo = try await todoService.markAsPending(id: todo.id)
            } else {
                updatedTodo = try await todoService.markAsCompleted(id: todo.id)
            }
            
            // Update the todo in our local array
            if let index = todos.firstIndex(where: { $0.id == todo.id }) {
                todos[index] = updatedTodo
            }
            
            // Reapply current filter
            await applyFilter(.all) // You might want to store the current filter
            
        } catch {
            errorMessage = "Failed to update todo: \(error.localizedDescription)"
            showingAlert = true
        }
    }
    
    func deleteTodos(at offsets: IndexSet) {
        Task {
            for index in offsets {
                let todo = filteredTodos[index]
                do {
                    try await todoService.deleteTodo(id: todo.id)
                    
                    // Remove from local arrays
                    if let originalIndex = todos.firstIndex(where: { $0.id == todo.id }) {
                        todos.remove(at: originalIndex)
                    }
                    filteredTodos.remove(at: index)
                    
                } catch {
                    errorMessage = "Failed to delete todo: \(error.localizedDescription)"
                    showingAlert = true
                }
            }
        }
    }
    
    func refreshTodos() async {
        await loadTodos()
    }
    
    // MARK: - Statistics
    func getTodoStats() async -> [String: Int] {
        do {
            return try await todoService.getTodoStats()
        } catch {
            print("Failed to get stats: \(error)")
            return [:]
        }
    }
    
    // MARK: - Computed Properties
    var completedCount: Int {
        todos.filter { $0.isCompleted }.count
    }
    
    var pendingCount: Int {
        todos.filter { $0.status == .pending }.count
    }
    
    var overdueCount: Int {
        todos.filter { $0.isOverdue }.count
    }
    
    var completionPercentage: Double {
        guard !todos.isEmpty else { return 0 }
        return Double(completedCount) / Double(todos.count) * 100
    }
}
