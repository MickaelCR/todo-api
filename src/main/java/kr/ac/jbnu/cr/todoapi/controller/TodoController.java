package kr.ac.jbnu.cr.todoapi.controller;

import jakarta.validation.Valid;
import kr.ac.jbnu.cr.todoapi.dto.request.CreateTodoRequest;
import kr.ac.jbnu.cr.todoapi.dto.request.UpdateTodoRequest;
import kr.ac.jbnu.cr.todoapi.dto.response.ApiResponse;
import kr.ac.jbnu.cr.todoapi.dto.response.ErrorResponse;
import kr.ac.jbnu.cr.todoapi.model.Todo;
import kr.ac.jbnu.cr.todoapi.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ========== GET ENDPOINTS ==========

    /**
     * GET /todos
     * Retrieve all todos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> getAllTodos() {
        String requestId = UUID.randomUUID().toString();
        List<Todo> todos = todoService.findAll();

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos");

        return ResponseEntity.ok(ApiResponse.success(todos, requestId, links));
    }

    /**
     * GET /todos/{id}
     * Retrieve a single todo by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getTodoById(@PathVariable Long id) {
        String requestId = UUID.randomUUID().toString();
        Optional<Todo> todoOptional = todoService.findById(id);

        if (todoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Todo todo = todoOptional.get();
        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/" + id);

        return ResponseEntity.ok(ApiResponse.success(todo, requestId, links));
    }

    // ========== POST ENDPOINTS ==========

    /**
     * POST /todos
     * Create a new todo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        String requestId = UUID.randomUUID().toString();
        Todo createdTodo = todoService.create(request);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/" + createdTodo.getId());

        URI location = URI.create("/todos/" + createdTodo.getId());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.success(createdTodo, requestId, links));
    }

    /**
     * POST /todos/batch
     * Create multiple todos at once
     */
    @PostMapping("/batch")
    public ResponseEntity<?> createTodosBatch(@Valid @RequestBody List<CreateTodoRequest> requests) {
        String requestId = UUID.randomUUID().toString();

        if (requests == null || requests.isEmpty()) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Invalid Request")
                    .status(400)
                    .detail("Request body must contain at least one todo.")
                    .instance("/todos/batch")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.badRequest().body(error);
        }

        List<Todo> createdTodos = todoService.createBatch(requests);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/batch");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdTodos, requestId, links));
    }

    // ========== PUT ENDPOINTS ==========

    /**
     * PUT /todos/{id}
     * Update a todo (full replacement)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id,
                                        @Valid @RequestBody UpdateTodoRequest request) {
        String requestId = UUID.randomUUID().toString();

        if (!todoService.existsById(id)) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Not Found")
                    .status(404)
                    .detail("Todo with id " + id + " not found.")
                    .instance("/todos/" + id)
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Optional<Todo> updatedTodo = todoService.update(id, request);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/" + id);

        return ResponseEntity.ok(ApiResponse.success(updatedTodo.get(), requestId, links));
    }

    /**
     * PUT /todos/{id}/complete
     * Mark a todo as completed
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTodo(@PathVariable Long id) {
        String requestId = UUID.randomUUID().toString();

        if (!todoService.existsById(id)) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Not Found")
                    .status(404)
                    .detail("Todo with id " + id + " not found.")
                    .instance("/todos/" + id + "/complete")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (todoService.isCompleted(id)) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Conflict")
                    .status(409)
                    .detail("Todo with id " + id + " is already completed.")
                    .instance("/todos/" + id + "/complete")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        Optional<Todo> completedTodo = todoService.complete(id);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/" + id);

        return ResponseEntity.ok(ApiResponse.success(completedTodo.get(), requestId, links));
    }

    // ========== DELETE ENDPOINTS ==========

    /**
     * DELETE /todos/{id}
     * Delete a todo by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        String requestId = UUID.randomUUID().toString();

        if (!todoService.existsById(id)) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Not Found")
                    .status(404)
                    .detail("Todo with id " + id + " not found.")
                    .instance("/todos/" + id)
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        todoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /todos/completed
     * Delete all completed todos
     */
    @DeleteMapping("/completed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCompletedTodos() {
        String requestId = UUID.randomUUID().toString();

        int deletedCount = todoService.deleteCompleted();

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", deletedCount);
        result.put("message", deletedCount + " completed todo(s) deleted.");

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos/completed");
        links.put("todos", "/todos");

        return ResponseEntity.ok(ApiResponse.success(result, requestId, links));
    }
}