package com.tasktracker.service;

import com.tasktracker.model.*;
import com.tasktracker.repo.TaskRepository;
import com.tasktracker.repo.AppUserRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    private final TaskRepository tasks;
    private final AppUserRepository users;
    private final JavaMailSender mailSender; // will be null-bean if not configured

    public NotificationService(TaskRepository tasks, AppUserRepository users,
            @Autowired(required = false) JavaMailSender mailSender) {
        this.tasks = tasks;
        this.users = users;
        this.mailSender = mailSender;
    }

    // Every hour
    @Scheduled(cron = "0 0 * * * *")
    public void notifyDueSoon() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Notify each user separately
        users.findAll().forEach(u -> {
            List<Task> dueSoon = tasks.findByOwnerAndDueDateBetween(u, today, tomorrow);
            dueSoon.removeIf(t -> t.getStatus() == Status.DONE);
            if (dueSoon.isEmpty())
                return;

            StringBuilder body = new StringBuilder("Tasks due soon:\n");
            for (Task t : dueSoon) {
                body.append("- ").append(t.getTitle()).append(" (").append(t.getDueDate()).append(") [")
                        .append(t.getPriority()).append("]\n");
            }

            if (u.getEmail() != null && !u.getEmail().isBlank() && mailSender != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(u.getEmail());
                    msg.setSubject("Task reminders");
                    msg.setText(body.toString());
                    mailSender.send(msg);
                } catch (Exception e) {
                    System.out.println("[Notify] Email failed for " + u.getUsername() + ": " + e.getMessage());
                    System.out.println(body);
                }
            } else {
                System.out.println("[Notify] (Console) for " + u.getUsername() + ":\n" + body);
            }
        });
    }

    // Manual notification trigger for testing
    public void sendTestNotification(AppUser user) {
        if (mailSender == null) {
            System.out.println("[TEST] Email service not configured");
            return;
        }
        
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            System.out.println("[TEST] User " + user.getUsername() + " has no email");
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("TaskTracker - Test Notification");
            msg.setText("Hello " + user.getUsername() + "!\n\n" +
                       "This is a test notification from your TaskTracker application.\n" +
                       "Email notifications are now working!\n\n" +
                       "You will receive notifications for:\n" +
                       "- Tasks due today or tomorrow\n" +
                       "- Overdue tasks\n" +
                       "- Weekly task summaries\n\n" +
                       "Happy task tracking!\n" +
                       "- TaskTracker Team");
            mailSender.send(msg);
            System.out.println("[TEST] Email sent successfully to " + user.getEmail());
        } catch (Exception e) {
            System.out.println("[TEST] Email failed: " + e.getMessage());
        }
    }

    // Send overdue task notifications  
    public void sendOverdueNotifications() {
        LocalDate today = LocalDate.now();
        
        users.findAll().forEach(u -> {
            List<Task> overdueTasks = tasks.findByOwner(u).stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != Status.DONE)
                .toList();
                
            if (overdueTasks.isEmpty()) return;

            StringBuilder body = new StringBuilder("⚠️ OVERDUE TASKS ALERT!\n\n");
            body.append("You have ").append(overdueTasks.size()).append(" overdue task(s):\n\n");
            
            for (Task t : overdueTasks) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(t.getDueDate(), today);
                body.append("📝 ").append(t.getTitle())
                    .append("\n   Due: ").append(t.getDueDate())
                    .append(" (").append(daysOverdue).append(" days overdue)")
                    .append("\n   Priority: ").append(t.getPriority())
                    .append("\n\n");
            }
            
            body.append("Please complete these tasks as soon as possible!\n");
            body.append("\nLogin to TaskTracker: http://localhost:8080\n");

            if (u.getEmail() != null && !u.getEmail().isBlank() && mailSender != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(u.getEmail());
                    msg.setSubject("TaskTracker - Overdue Tasks Alert ⚠️");
                    msg.setText(body.toString());
                    mailSender.send(msg);
                    System.out.println("[OVERDUE] Email sent to " + u.getUsername());
                } catch (Exception e) {
                    System.out.println("[OVERDUE] Email failed for " + u.getUsername() + ": " + e.getMessage());
                }
            }
        });
    }

    // Weekly summary (every Monday at 9 AM)
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklySummary() {
        users.findAll().forEach(u -> {
            List<Task> allTasks = tasks.findByOwner(u);
            long totalTasks = allTasks.size();
            long completedTasks = allTasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
            long pendingTasks = totalTasks - completedTasks;
            
            if (totalTasks == 0) return;

            StringBuilder body = new StringBuilder("📊 WEEKLY TASK SUMMARY\n\n");
            body.append("Hello ").append(u.getUsername()).append("!\n\n");
            body.append("Here's your weekly task summary:\n\n");
            body.append("📈 Total Tasks: ").append(totalTasks).append("\n");
            body.append("✅ Completed: ").append(completedTasks).append("\n");
            body.append("⏳ Pending: ").append(pendingTasks).append("\n");
            
            if (totalTasks > 0) {
                int completionRate = (int) ((completedTasks * 100) / totalTasks);
                body.append("📊 Completion Rate: ").append(completionRate).append("%\n\n");
                
                if (completionRate >= 80) {
                    body.append("🎉 Excellent work! You're very productive!\n");
                } else if (completionRate >= 60) {
                    body.append("👍 Good progress! Keep it up!\n");
                } else {
                    body.append("💪 Let's focus on completing more tasks this week!\n");
                }
            }
            
            body.append("\nStay productive!\n");
            body.append("Login to TaskTracker: http://localhost:8080\n");

            if (u.getEmail() != null && !u.getEmail().isBlank() && mailSender != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(u.getEmail());
                    msg.setSubject("TaskTracker - Weekly Summary 📊");
                    msg.setText(body.toString());
                    mailSender.send(msg);
                    System.out.println("[WEEKLY] Summary sent to " + u.getUsername());
                } catch (Exception e) {
                    System.out.println("[WEEKLY] Email failed for " + u.getUsername() + ": " + e.getMessage());
                }
            }
        });
    }
    
    // Send welcome email to new users
    public void sendWelcomeEmail(AppUser user) {
        if (mailSender == null) {
            System.out.println("[WELCOME] Email service not configured");
            return;
        }
        
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            System.out.println("[WELCOME] User " + user.getUsername() + " has no email");
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("Welcome to TaskTracker! 🎉");
            
            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getUsername()).append("!\n\n");
            body.append("🎉 Welcome to TaskTracker - Your Personal Task Management Hub!\n\n");
            body.append("We're excited to have you on board! TaskTracker will help you:\n");
            body.append("✅ Organize your tasks efficiently\n");
            body.append("📅 Set priorities and due dates\n");
            body.append("📂 Categorize your work\n");
            body.append("📧 Receive helpful notifications\n");
            body.append("📊 Track your productivity\n\n");
            body.append("🚀 Getting Started:\n");
            body.append("1. Login to your dashboard: http://localhost:8080\n");
            body.append("2. Create your first task\n");
            body.append("3. Set up categories for better organization\n");
            body.append("4. Enable email notifications for reminders\n\n");
            body.append("💡 Pro Tips:\n");
            body.append("• Use priority levels to focus on what matters most\n");
            body.append("• Set realistic due dates to stay on track\n");
            body.append("• Check your weekly summaries to improve productivity\n\n");
            body.append("If you have any questions or need help, don't hesitate to reach out!\n\n");
            body.append("Happy task tracking! 📝\n");
            body.append("- The TaskTracker Team");
            
            msg.setText(body.toString());
            mailSender.send(msg);
            System.out.println("[WELCOME] Email sent successfully to " + user.getEmail());
        } catch (Exception e) {
            System.out.println("[WELCOME] Email failed: " + e.getMessage());
        }
    }
}
