package com.tasktracker.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktracker.model.AppUser;
import com.tasktracker.model.Priority;
import com.tasktracker.model.Status;
import com.tasktracker.model.Task;
import com.tasktracker.repo.AppUserRepository;
import com.tasktracker.repo.CategoryRepository;
import com.tasktracker.repo.TaskRepository;

@Controller
public class TaskPageController {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;

    public TaskPageController(TaskRepository taskRepository, CategoryRepository categoryRepository, AppUserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }
//get current logged in user
    private AppUser getCurrentUser(User principal) {
        return userRepository.findByUsername(principal.getUsername()).orElseThrow();
    }
//handle requests to different pages
    @GetMapping("/pages/tasks")
    public String tasksPage(@AuthenticationPrincipal User user, 
                            @RequestParam(required = false) String filter, 
                            Model model) {
        AppUser currentUser = getCurrentUser(user);
        List<Task> tasks = taskRepository.findByOwner(currentUser);
        //fetch all the task of user 
        // Apply filters
        if ("high-priority".equals(filter)) {
            tasks = tasks.stream()
                .filter(t -> t.getPriority() == Priority.HIGH)
                .collect(Collectors.toList());
        } else if ("pending".equals(filter)) {
            tasks = tasks.stream()
                .filter(t -> t.getStatus() == Status.PENDING)
                .collect(Collectors.toList());
        } else if ("completed".equals(filter)) {
            tasks = tasks.stream()
                .filter(t -> t.getStatus() == Status.DONE)
                .collect(Collectors.toList());
        } else if ("due-today".equals(filter)) {
            LocalDate today = LocalDate.now();
            tasks = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().equals(today))
                .collect(Collectors.toList());
        }
        //add data to models for the template
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("username", user.getUsername());
        return "tasks";
    }

    @GetMapping("/pages/tasks/new")
    public String newTaskPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        return "task-form";
    }

    @GetMapping("/pages/tasks/edit")
    public String editTaskPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        return "task-form";
    }

    @GetMapping("/pages/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "categories";
    }

    @GetMapping("/pages/analytics")
    public String analyticsPage(@AuthenticationPrincipal User user, Model model) {
        AppUser currentUser = getCurrentUser(user);
        List<Task> tasks = taskRepository.findByOwner(currentUser);
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        long pendingTasks = totalTasks - completedTasks;
        int productivityRate = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;
        
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("productivityRate", productivityRate);
        model.addAttribute("username", user.getUsername());
        return "analytics";
    }

    @GetMapping("/pages/notifications")
    public String notificationsPage(@AuthenticationPrincipal User user, Model model) {
        AppUser currentUser = getCurrentUser(user);
        List<Task> tasks = taskRepository.findByOwner(currentUser);
        
        LocalDate today = LocalDate.now();
        
        List<Task> overdueTasks = tasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != Status.DONE)
            .collect(Collectors.toList());
            
        List<Task> dueTodayTasks = tasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().equals(today) && t.getStatus() != Status.DONE)
            .collect(Collectors.toList());
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        long pendingTasks = totalTasks - completedTasks;
        int productivityRate = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;
        
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("dueTodayTasks", dueTodayTasks);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("productivityRate", productivityRate);
        return "notifications";
    }
}
