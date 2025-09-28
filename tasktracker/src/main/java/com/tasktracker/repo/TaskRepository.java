package com.tasktracker.repo;

import com.tasktracker.model.Task;
import com.tasktracker.model.AppUser;
import com.tasktracker.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwner(AppUser owner);
    List<Task> findByOwnerAndStatus(AppUser owner, Status status);
    List<Task> findByOwnerAndDueDateBetween(AppUser owner, LocalDate start, LocalDate end);
}
