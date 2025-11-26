package kr.ac.jbnu.cr.todoapi.exception;

/**
 * Exception thrown when trying to complete an already completed todo
 */
public class TodoAlreadyCompletedException extends RuntimeException {

    private final Long todoId;

    public TodoAlreadyCompletedException(Long todoId) {
        super("Todo with id " + todoId + " is already completed.");
        this.todoId = todoId;
    }

    public Long getTodoId() {
        return todoId;
    }
}