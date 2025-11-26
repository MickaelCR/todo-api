package kr.ac.jbnu.cr.todoapi.controller;

import kr.ac.jbnu.cr.todoapi.dto.response.ApiResponse;
import kr.ac.jbnu.cr.todoapi.model.Todo;
import kr.ac.jbnu.cr.todoapi.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * GET /todos
     * Retrieve all todos
     *
     * @return 200 OK with list of todos
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
     *
     * @param id the todo ID
     * @return 200 OK with todo, or 404 Not Found
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
}