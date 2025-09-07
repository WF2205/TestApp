import SwiftUI

struct TodoListView: View {
    @StateObject private var viewModel = TodoListViewModel()
    @State private var showingCreateTodo = false
    @State private var selectedFilter: TodoFilter = .all
    
    var body: some View {
        NavigationView {
            VStack {
                // Filter Picker
                Picker("Filter", selection: $selectedFilter) {
                    ForEach(TodoFilter.allCases, id: \.self) { filter in
                        Text(filter.displayName).tag(filter)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .onChange(of: selectedFilter) { _ in
                    Task {
                        await viewModel.applyFilter(selectedFilter)
                    }
                }
                
                // Todo List
                if viewModel.isLoading {
                    Spacer()
                    ProgressView("Loading todos...")
                    Spacer()
                } else if viewModel.filteredTodos.isEmpty {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "checklist")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        
                        Text("No todos found")
                            .font(.title2)
                            .fontWeight(.medium)
                        
                        Text("Tap the + button to create your first todo")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    Spacer()
                } else {
                    List {
                        ForEach(viewModel.filteredTodos) { todo in
                            TodoRowView(todo: todo) {
                                Task {
                                    await viewModel.toggleTodoCompletion(todo)
                                }
                            }
                        }
                        .onDelete(perform: viewModel.deleteTodos)
                    }
                    .refreshable {
                        await viewModel.loadTodos()
                    }
                }
            }
            .navigationTitle("My Todos")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingCreateTodo = true
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingCreateTodo) {
                CreateTodoView {
                    Task {
                        await viewModel.loadTodos()
                    }
                }
            }
            .alert("Error", isPresented: $viewModel.showingAlert) {
                Button("OK") { }
            } message: {
                Text(viewModel.errorMessage ?? "An unknown error occurred")
            }
        }
        .task {
            await viewModel.loadTodos()
        }
    }
}

enum TodoFilter: CaseIterable {
    case all
    case pending
    case inProgress
    case completed
    case overdue
    
    var displayName: String {
        switch self {
        case .all: return "All"
        case .pending: return "Pending"
        case .inProgress: return "In Progress"
        case .completed: return "Completed"
        case .overdue: return "Overdue"
        }
    }
}

#Preview {
    TodoListView()
}
