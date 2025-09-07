import SwiftUI

struct TodoRowView: View {
    let todo: Todo
    let onToggle: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            // Completion Toggle
            Button(action: onToggle) {
                Image(systemName: todo.isCompleted ? "checkmark.circle.fill" : "circle")
                    .font(.title2)
                    .foregroundColor(todo.isCompleted ? .green : .gray)
            }
            .buttonStyle(PlainButtonStyle())
            
            // Todo Content
            VStack(alignment: .leading, spacing: 6) {
                // Title
                Text(todo.title)
                    .font(.headline)
                    .strikethrough(todo.isCompleted)
                    .foregroundColor(todo.isCompleted ? .secondary : .primary)
                    .lineLimit(2)
                
                // Description
                if let description = todo.description, !description.isEmpty {
                    Text(description)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                }
                
                // Tags and Status
                HStack {
                    // Status Badge
                    HStack(spacing: 4) {
                        Image(systemName: todo.status.icon)
                            .font(.caption)
                        Text(todo.status.displayName)
                            .font(.caption)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(todo.status.color).opacity(0.2))
                    .foregroundColor(Color(todo.status.color))
                    .cornerRadius(8)
                    
                    // Priority Badge
                    HStack(spacing: 4) {
                        Image(systemName: todo.priority.icon)
                            .font(.caption)
                        Text(todo.priority.displayName)
                            .font(.caption)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(todo.priority.color).opacity(0.2))
                    .foregroundColor(Color(todo.priority.color))
                    .cornerRadius(8)
                    
                    Spacer()
                    
                    // Overdue Indicator
                    if todo.isOverdue {
                        HStack(spacing: 4) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.caption)
                            Text("Overdue")
                                .font(.caption)
                        }
                        .foregroundColor(.red)
                    }
                }
                
                // Due Date
                if let dueDate = todo.formattedDueDate {
                    HStack {
                        Image(systemName: "calendar")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(dueDate)
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                }
                
                // Tags
                if let tags = todo.tags, !tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(tags, id: \.self) { tag in
                                Text("#\(tag)")
                                    .font(.caption)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.blue.opacity(0.1))
                                    .foregroundColor(.blue)
                                    .cornerRadius(4)
                            }
                        }
                        .padding(.horizontal, 1)
                    }
                }
            }
            
            Spacer()
        }
        .padding(.vertical, 8)
        .contentShape(Rectangle())
    }
}

#Preview {
    List {
        TodoRowView(todo: Todo.samples[0]) { }
        TodoRowView(todo: Todo.samples[1]) { }
        TodoRowView(todo: Todo.samples[2]) { }
    }
}
