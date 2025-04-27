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

    public Task createTask(TaskDTO taskDTO) {

        Task newTask = new Task(taskDTO.title(), taskDTO.description(), taskDTO.status(), getLoggedInUser());
        return taskRepository.save(newTask);
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
        return taskRepository.save(task);

    }

    public void deleteTask(long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new ApiException("Cannot find Task", "TASK_NOT_FOUND"));
        User loggedInUser = getLoggedInUser();

        if (!task.getUser().getId().equals(loggedInUser.getId())
                && !loggedInUser.getRole().contains("ADMIN")) {
            throw new ApiException("Unauthorized access to task", "UNAUTHORIZED");
        }

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
        return task;
    }

    public Page<Task> getTasksByUser(int pageNum, int pageSize) {
        User loggedInUser = getLoggedInUser();
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return taskRepository.findByUserId(loggedInUser.getId(), pageable);
    }

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", "UNAUTHORIZED"));
    }
}
