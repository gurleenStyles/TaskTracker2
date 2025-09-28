package com.tasktracker.service;

import com.tasktracker.model.*;
import com.tasktracker.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository,
            AppUserRepository appUserRepository,
            CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get current user by username
     */
    public AppUser getCurrentUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Get all tasks for a user
     */
    public List<Task> getUserTasks(String username) {
        AppUser user = getCurrentUser(username);
        return taskRepository.findByOwner(user);
    }

    /**
     * Get tasks by user and status
     */
    public List<Task> getUserTasksByStatus(String username, Status status) {
        AppUser user = getCurrentUser(username);
        return taskRepository.findByOwnerAndStatus(user, status);
    }

    /**
     * Get tasks by user within date range
     */
    public List<Task> getUserTasksByDateRange(String username, LocalDate start, LocalDate end) {
        AppUser user = getCurrentUser(username);
        return taskRepository.findByOwnerAndDueDateBetween(user, start, end);
    }

    /**
     * Create a new task
     */
    public Task createTask(String username, String title, String description,
            Priority priority, Status status, String dueDate, Long categoryId) {
        AppUser owner = getCurrentUser(username);

        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        Task task = new Task();
        task.setTitle(title.trim());
        task.setDescription(description);
        task.setOwner(owner);

        // Set optional fields with defaults
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setStatus(status != null ? status : Status.PENDING);

        // Parse and set due date if provided
        if (dueDate != null && !dueDate.trim().isEmpty()) {
            try {
                task.setDueDate(LocalDate.parse(dueDate));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format.");
            }
        }

        // Set category if provided
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }

        return taskRepository.save(task);
    }

    /**
     * Update an existing task
     */
    public Task updateTask(String username, Long taskId, String title, String description,
            Priority priority, Status status, String dueDate, Long categoryId) {
        AppUser currentUser = getCurrentUser(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Check ownership
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied. You can only update your own tasks.");
        }

        // Update fields if provided
        if (title != null && !title.trim().isEmpty()) {
            task.setTitle(title.trim());
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (status != null) {
            task.setStatus(status);
        }

        // Update due date if provided
        if (dueDate != null && !dueDate.trim().isEmpty()) {
            try {
                task.setDueDate(LocalDate.parse(dueDate));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format.");
            }
        }

        // Update category if provided
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }

        return taskRepository.save(task);
    }

    /**
     * Delete a task
     */
    public void deleteTask(String username, Long taskId) {
        AppUser currentUser = getCurrentUser(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Check ownership
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied. You can only delete your own tasks.");
        }

        taskRepository.deleteById(taskId);
    }

    /**
     * Get a single task by ID with ownership check
     */
    public Optional<Task> getTaskById(String username, Long taskId) {
        AppUser currentUser = getCurrentUser(username);

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            // Check ownership
            if (!task.getOwner().getId().equals(currentUser.getId())) {
                return Optional.empty(); // User doesn't own this task
            }
        }
        return taskOpt;
    }

    /**
     * Check if a task belongs to a user
     */
    public boolean isTaskOwner(String username, Long taskId) {
        AppUser currentUser = getCurrentUser(username);

        return taskRepository.findById(taskId)
                .map(task -> task.getOwner().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}