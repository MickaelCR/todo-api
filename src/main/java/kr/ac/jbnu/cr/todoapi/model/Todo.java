package kr.ac.jbnu.cr.todoapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean done;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}