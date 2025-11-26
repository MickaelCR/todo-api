package kr.ac.jbnu.cr.todoapi.exception;

/**
 * Exception thrown when a todo is not found
 */
public class TodoNotFoundException extends RuntimeException {

    private final Long todoId;

    public TodoNotFoundException(Long todoId) {
        super("Todo with id " + todoId + " not found.");
        this.todoId = todoId;
    }

    public Long getTodoId() {
        return todoId;
    }
}