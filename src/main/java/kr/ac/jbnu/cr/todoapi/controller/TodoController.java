package kr.ac.jbnu.cr.todoapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.jbnu.cr.todoapi.dto.request.CreateTodoRequest;
import kr.ac.jbnu.cr.todoapi.dto.request.UpdateTodoRequest;
import kr.ac.jbnu.cr.todoapi.dto.response.ApiResponse;
import kr.ac.jbnu.cr.todoapi.dto.response.ErrorResponse;
import kr.ac.jbnu.cr.todoapi.model.Todo;
import kr.ac.jbnu.cr.todoapi.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/todos")
@Tag(name = "Todo", description = "Todo management APIs")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ========== GET ENDPOINTS ==========

    @Operation(summary = "Get all todos", description = "Retrieve a list of all todos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved all todos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> getAllTodos() {
        String requestId = UUID.randomUUID().toString();
        List<Todo> todos = todoService.findAll();

        Map<String, String> links = new HashMap<>();
        links.put("self", "/todos");

        return ResponseEntity.ok(ApiResponse.success(todos, requestId, links));
    }

    @Operation(summary = "Get todo by ID", description = "Retrieve a single todo by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved the todo"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getTodoById(
            @Parameter(description = "ID of the todo to retrieve") @PathVariable Long id) {
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

    @Operation(summary = "Create a new todo", description = "Create a new todo item")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Todo created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Create multiple todos", description = "Create multiple todo items at once")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Todos created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Update a todo", description = "Update an existing todo by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Todo updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            @Parameter(description = "ID of the todo to update") @PathVariable Long id,
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

    @Operation(summary = "Mark todo as completed", description = "Mark a todo as completed by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Todo marked as completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Todo already completed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTodo(
            @Parameter(description = "ID of the todo to complete") @PathVariable Long id) {
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

    @Operation(summary = "Delete a todo", description = "Delete a todo by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @Parameter(description = "ID of the todo to delete") @PathVariable Long id) {
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

    @Operation(summary = "Delete all completed todos", description = "Delete all todos that are marked as completed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Completed todos deleted successfully")
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