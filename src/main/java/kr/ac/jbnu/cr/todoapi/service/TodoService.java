package kr.ac.jbnu.cr.todoapi.service;

import kr.ac.jbnu.cr.todoapi.dto.request.CreateTodoRequest;
import kr.ac.jbnu.cr.todoapi.dto.request.UpdateTodoRequest;
import kr.ac.jbnu.cr.todoapi.model.Todo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TodoService {

    // In-memory storage (HashMap)
    private final Map<Long, Todo> todoStorage = new HashMap<>();

    // Auto-incremented ID generator
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Retrieve all todos
     * @return list of all todos
     */
    public List<Todo> findAll() {
        return new ArrayList<>(todoStorage.values());
    }

    /**
     * Retrieve a todo by ID
     * @param id the todo ID
     * @return optional containing the todo if found
     */
    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(todoStorage.get(id));
    }

    /**
     * Create a new todo
     * @param request the creation request
     * @return the created todo
     */
    public Todo create(CreateTodoRequest request) {
        Long id = idGenerator.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();

        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setDueDate(request.getDueDate());
        todo.setDone(false);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);

        todoStorage.put(id, todo);
        return todo;
    }

    /**
     * Create multiple todos (batch operation)
     * @param requests list of creation requests
     * @return list of created todos
     */
    public List<Todo> createBatch(List<CreateTodoRequest> requests) {
        List<Todo> createdTodos = new ArrayList<>();
        for (CreateTodoRequest request : requests) {
            createdTodos.add(create(request));
        }
        return createdTodos;
    }

    /**
     * Update a todo (full replacement)
     * @param id the todo ID
     * @param request the update request
     * @return optional containing the updated todo if found
     */
    public Optional<Todo> update(Long id, UpdateTodoRequest request) {
        Todo existingTodo = todoStorage.get(id);

        if (existingTodo == null) {
            return Optional.empty();
        }

        existingTodo.setTitle(request.getTitle());
        existingTodo.setDescription(request.getDescription());
        existingTodo.setDueDate(request.getDueDate());
        if (request.getDone() != null) {
            existingTodo.setDone(request.getDone());
        }
        existingTodo.setUpdatedAt(LocalDateTime.now());

        todoStorage.put(id, existingTodo);
        return Optional.of(existingTodo);
    }

    /**
     * Mark a todo as completed
     * @param id the todo ID
     * @return optional containing the completed todo if found and not already completed
     */
    public Optional<Todo> complete(Long id) {
        Todo existingTodo = todoStorage.get(id);

        if (existingTodo == null) {
            return Optional.empty();
        }

        // Check if already completed (for 409 Conflict response)
        if (existingTodo.isDone()) {
            return Optional.empty();
        }

        existingTodo.setDone(true);
        existingTodo.setUpdatedAt(LocalDateTime.now());

        todoStorage.put(id, existingTodo);
        return Optional.of(existingTodo);
    }

    /**
     * Check if a todo exists
     * @param id the todo ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return todoStorage.containsKey(id);
    }

    /**
     * Check if a todo is already completed
     * @param id the todo ID
     * @return true if completed, false otherwise
     */
    public boolean isCompleted(Long id) {
        Todo todo = todoStorage.get(id);
        return todo != null && todo.isDone();
    }

    /**
     * Delete a todo by ID
     * @param id the todo ID
     * @return true if deleted, false if not found
     */
    public boolean delete(Long id) {
        return todoStorage.remove(id) != null;
    }

    /**
     * Delete all completed todos
     * @return number of deleted todos
     */
    public int deleteCompleted() {
        List<Long> completedIds = todoStorage.entrySet().stream()
                .filter(entry -> entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .toList();

        for (Long id : completedIds) {
            todoStorage.remove(id);
        }

        return completedIds.size();
    }

    /**
     * Count total number of todos
     * @return total count
     */
    public long count() {
        return todoStorage.size();
    }
}