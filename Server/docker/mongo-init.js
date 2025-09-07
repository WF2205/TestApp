// MongoDB initialization script
// This script runs when the MongoDB container starts for the first time

// Switch to the todolist database
db = db.getSiblingDB('todolist');

// Create collections with validation
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['username', 'email', 'passwordEnabled', 'active', 'roles'],
      properties: {
        username: {
          bsonType: 'string',
          minLength: 3,
          maxLength: 50
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'
        },
        passwordEnabled: {
          bsonType: 'bool'
        },
        active: {
          bsonType: 'bool'
        },
        roles: {
          bsonType: 'array',
          items: {
            bsonType: 'string'
          }
        }
      }
    }
  }
});

db.createCollection('todos', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['title', 'userId', 'status', 'priority', 'createdAt', 'updatedAt'],
      properties: {
        title: {
          bsonType: 'string',
          minLength: 1,
          maxLength: 200
        },
        description: {
          bsonType: 'string',
          maxLength: 1000
        },
        userId: {
          bsonType: 'string'
        },
        status: {
          bsonType: 'string',
          enum: ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
        },
        priority: {
          bsonType: 'string',
          enum: ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
        },
        dueDate: {
          bsonType: 'date'
        },
        completedAt: {
          bsonType: 'date'
        },
        createdAt: {
          bsonType: 'date'
        },
        updatedAt: {
          bsonType: 'date'
        },
        tags: {
          bsonType: 'array',
          items: {
            bsonType: 'string'
          }
        },
        attachments: {
          bsonType: 'array',
          items: {
            bsonType: 'string'
          }
        },
        isDeleted: {
          bsonType: 'bool'
        },
        deletedAt: {
          bsonType: 'date'
        }
      }
    }
  }
});

db.createCollection('notifications', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'title', 'message', 'type', 'isRead', 'createdAt'],
      properties: {
        userId: {
          bsonType: 'string'
        },
        title: {
          bsonType: 'string',
          minLength: 1,
          maxLength: 100
        },
        message: {
          bsonType: 'string',
          minLength: 1,
          maxLength: 500
        },
        type: {
          bsonType: 'string',
          enum: ['WELCOME', 'TODO_DUE', 'TODO_OVERDUE', 'REMINDER']
        },
        isRead: {
          bsonType: 'bool'
        },
        createdAt: {
          bsonType: 'date'
        }
      }
    }
  }
});

// Create indexes for better performance
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "githubId": 1 }, { unique: true, sparse: true });

db.todos.createIndex({ "userId": 1 });
db.todos.createIndex({ "userId": 1, "status": 1 });
db.todos.createIndex({ "userId": 1, "isDeleted": 1 });
db.todos.createIndex({ "dueDate": 1 });
db.todos.createIndex({ "createdAt": 1 });
db.todos.createIndex({ "priority": 1 });

db.notifications.createIndex({ "userId": 1 });
db.notifications.createIndex({ "userId": 1, "isRead": 1 });
db.notifications.createIndex({ "createdAt": 1 });

print('MongoDB initialization completed successfully!');