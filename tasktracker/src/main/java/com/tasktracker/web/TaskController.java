package com.tasktracker.web;

import com.tasktracker.model.*;
import com.tasktracker.service.TaskService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

record TaskCreateRequest(
        @NotBlank String title,
        String description,
        Priority priority,
        Status status,
        String dueDate, // ISO date "2025-09-01"
        Long categoryId) {
}

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Returns all the tasks of the current logged in user
    @GetMapping
    public List<Task> myTasks(@AuthenticationPrincipal User principal) {
        return taskService.getUserTasks(principal.getUsername());
    }

    // Handles the create api - creates a new task
    @PostMapping
    public Task create(@AuthenticationPrincipal User principal, @RequestBody TaskCreateRequest req) {
        return taskService.createTask(
                principal.getUsername(),
                req.title(),
                req.description(),
                req.priority(),
                req.status(),
                req.dueDate(),
                req.categoryId());
    }

    // Finds the task by id, updates the task with the new values
    @PutMapping("/{id}")
    public Task update(@AuthenticationPrincipal User principal, @PathVariable Long id,
            @RequestBody TaskCreateRequest req) {
        return taskService.updateTask(
                principal.getUsername(),
                id,
                req.title(),
                req.description(),
                req.priority(),
                req.status(),
                req.dueDate(),
                req.categoryId());
    }

    // Handles the delete api
    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal User principal, @PathVariable Long id) {
        taskService.deleteTask(principal.getUsername(), id);
    }
}
// User logs in → SecurityContext stores the principal (User).
// Frontend calls /tasks → returns only their tasks.
// Frontend POSTs /tasks → creates a new task with the logged-in user as the
// owner.
// Frontend PUTs /tasks/{id} → updates only if the logged-in user is the owner.
// Frontend DELETEs /tasks/{id} → deletes only if the logged-in user is the
// owner.