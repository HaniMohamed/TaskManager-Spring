package com.taskmanager.service;


import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Task createTask(TaskDTO taskDTO) {

        Task newTask = new Task(taskDTO.title(), taskDTO.description(), taskDTO.status(), getLoggedInUser());
        Task savedTask = taskRepository.save(newTask);
        auditLogService.logAction("TASK_CREATED", "Task ID: " + savedTask.getId() + ", Title: " + savedTask.getTitle()
                + ", Description: " + savedTask.getDescription());
        return savedTask;
    }

    public Task updateTask(TaskDTO taskDTO, long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new ApiException("Task not found", "TASK_NOT_FOUND"));

        User loggedInUser = getLoggedInUser();
        if (!task.getUser().getUsername().equals(loggedInUser.getUsername())
                && !loggedInUser.getRole().contains("ROLE_ADMIN")) {
            throw new ApiException("Unauthorized access to task", "UNAUTHORIZED");
        }

        task.setTitle(taskDTO.title());
        task.setStatus(taskDTO.status());
        task.setDescription(taskDTO.description());

        Task savedTask = taskRepository.save(task);

        auditLogService.logAction("TASK_UPDATED", "Task ID: " + savedTask.getId() + ", Title: " + savedTask.getTitle()
                + ", Description: " + savedTask.getDescription());
        return savedTask;

    }

    public void deleteTask(long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new ApiException("Cannot find Task", "TASK_NOT_FOUND"));
        User loggedInUser = getLoggedInUser();

        if (!task.getUser().getId().equals(loggedInUser.getId())
                && !loggedInUser.getRole().contains("ADMIN")) {
            throw new ApiException("Unauthorized access to task", "UNAUTHORIZED");
        }

        auditLogService.logAction("TASK_DELETED", "Task ID: " + task.getId() + ", Title: " + task.getTitle()
                + ", Description: " + task.getDescription());
        taskRepository.delete(task);
    }

    public Task getTaskById(long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Cannot find Task", "TASK_NOT_FOUND"));

        User loggedInUser = getLoggedInUser();
        if (!task.getUser().getId().equals(loggedInUser.getId())
                && !loggedInUser.getRole().contains("ADMIN")) {
            throw new ApiException("Unauthorized access to task", "UNAUTHORIZED");
        }

        auditLogService.logAction("TASK_FETCHED_BY_ID", "Task ID: " + task.getId() + ", Title: " + task.getTitle()
                + ", Description: " + task.getDescription());
        return task;
    }

    public Page<Task> getTasksByUser(int pageNum, int pageSize) {
        User loggedInUser = getLoggedInUser();
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        auditLogService.logAction("USER_FETCHED_ALL_TASKS", "User ID: " + loggedInUser.getId() + ", PageNum: " + pageNum
                + ", PageSize: " + pageSize);
        return taskRepository.findByUserId(loggedInUser.getId(), pageable);
    }

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", "UNAUTHORIZED"));
    }
}
