package com.tasktracker.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.model.AppUser;
import com.tasktracker.repo.AppUserRepository;
import com.tasktracker.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AppUserRepository userRepository;

    public NotificationController(NotificationService notificationService, AppUserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/test")
    public String sendTestEmail(@AuthenticationPrincipal User principal) {
        try {
            AppUser currentUser = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            notificationService.sendTestNotification(currentUser);
            return "Test notification sent to " + currentUser.getEmail();
        } catch (Exception e) {
            return "Error sending notification: " + e.getMessage();
        }
    }

    @PostMapping("/overdue")
    public String sendOverdueNotifications(@AuthenticationPrincipal User principal) {
        try {
            notificationService.sendOverdueNotifications();
            return "Overdue notifications sent to all users";
        } catch (Exception e) {
            return "Error sending overdue notifications: " + e.getMessage();
        }
    }

    @PostMapping("/weekly-summary")
    public String sendWeeklySummary(@AuthenticationPrincipal User principal) {
        try {
            notificationService.sendWeeklySummary();
            return "Weekly summary sent to all users";
        } catch (Exception e) {
            return "Error sending weekly summary: " + e.getMessage();
        }
    }

    @GetMapping("/status")
    public String getNotificationStatus() {
        return "Notification service is active. Available endpoints:\n" +
               "POST /api/notifications/test - Send test email\n" +
               "POST /api/notifications/overdue - Send overdue task alerts\n" +
               "POST /api/notifications/weekly-summary - Send weekly summary\n" +
               "\nScheduled notifications:\n" +
               "- Due soon tasks: Every hour\n" +
               "- Weekly summary: Every Monday at 9 AM";
    }
}