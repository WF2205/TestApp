import Foundation

class TodoService: ObservableObject {
    static let shared = TodoService()
    
    private let config = NetworkConfigManager.shared
    
    private init() {}
    
    // MARK: - Todo CRUD Operations
    func getAllTodos() async throws -> [Todo] {
        let todos: [Todo] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.getAll,
            responseType: [Todo].self
        )
        return todos
    }
    
    func getTodo(id: String) async throws -> Todo {
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.getById,
            responseType: Todo.self,
            parameters: ["id": id]
        )
        return todo
    }
    
    func createTodo(title: String, description: String?, priority: TodoPriority, dueDate: Date?) async throws -> Todo {
        var parameters: [String: Any] = [
            "title": title,
            "priority": priority.rawValue
        ]
        
        if let description = description, !description.isEmpty {
            parameters["description"] = description
        }
        
        if let dueDate = dueDate {
            let formatter = ISO8601DateFormatter()
            parameters["dueDate"] = formatter.string(from: dueDate)
        }
        
        let jsonData = try JSONSerialization.data(withJSONObject: parameters)
        
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.create,
            method: .POST,
            body: jsonData,
            responseType: Todo.self
        )
        return todo
    }
    
    func updateTodo(
        id: String,
        title: String,
        description: String?,
        status: TodoStatus,
        priority: TodoPriority,
        dueDate: Date?
    ) async throws -> Todo {
        var parameters: [String: Any] = [
            "title": title,
            "status": status.rawValue,
            "priority": priority.rawValue
        ]
        
        if let description = description, !description.isEmpty {
            parameters["description"] = description
        }
        
        if let dueDate = dueDate {
            let formatter = ISO8601DateFormatter()
            parameters["dueDate"] = formatter.string(from: dueDate)
        }
        
        let jsonData = try JSONSerialization.data(withJSONObject: parameters)
        
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.update,
            method: .PUT,
            body: jsonData,
            responseType: Todo.self,
            parameters: ["id": id]
        )
        return todo
    }
    
    func deleteTodo(id: String) async throws {
        let _: [String: String] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.delete,
            method: .DELETE,
            responseType: [String: String].self,
            parameters: ["id": id]
        )
    }
    
    // MARK: - Todo Status Operations
    func markAsCompleted(id: String) async throws -> Todo {
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.complete,
            method: .PUT,
            responseType: Todo.self,
            parameters: ["id": id]
        )
        return todo
    }
    
    func markAsPending(id: String) async throws -> Todo {
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.pending,
            method: .PUT,
            responseType: Todo.self,
            parameters: ["id": id]
        )
        return todo
    }
    
    func markAsInProgress(id: String) async throws -> Todo {
        let todo: Todo = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.inProgress,
            method: .PUT,
            responseType: Todo.self,
            parameters: ["id": id]
        )
        return todo
    }
    
    // MARK: - Filtered Queries
    func getTodosByStatus(_ status: TodoStatus) async throws -> [Todo] {
        let todos: [Todo] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.byStatus,
            responseType: [Todo].self,
            parameters: ["status": status.rawValue]
        )
        return todos
    }
    
    func getTodosByPriority(_ priority: TodoPriority) async throws -> [Todo] {
        let todos: [Todo] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.byPriority,
            responseType: [Todo].self,
            parameters: ["priority": priority.rawValue]
        )
        return todos
    }
    
    func getOverdueTodos() async throws -> [Todo] {
        let todos: [Todo] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.overdue,
            responseType: [Todo].self
        )
        return todos
    }
    
    func getTodosDueSoon(hoursAhead: Int = 24) async throws -> [Todo] {
        let todos: [Todo] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.dueSoon,
            responseType: [Todo].self,
            parameters: ["hoursAhead": String(hoursAhead)]
        )
        return todos
    }
    
    // MARK: - Statistics
    func getTodoStats() async throws -> [String: Int] {
        let stats: [String: Int] = try await APIClient.shared.request(
            endpoint: config.endpoints.todos.stats,
            responseType: [String: Int].self
        )
        return stats
    }
}
