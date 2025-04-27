package com.taskmanager.service;


import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        User loggedInUser = getLoggedInUser();
        if (!task.getUser().getUsername().equals(loggedInUser.getUsername())
                && !loggedInUser.getRole().contains("ROLE_ADMIN")) {
            throw new IllegalArgumentException("Unauthorized access to task");
        }

        task.setTitle(taskDTO.title());
        task.setStatus(taskDTO.status());
        task.setDescription(taskDTO.description());
        return taskRepository.save(task);

    }

    public void deleteTask(long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Task"));
        User loggedInUser = getLoggedInUser();

        if (!task.getUser().getId().equals(loggedInUser.getId())
                && !loggedInUser.getRole().contains("ADMIN")) {
            throw new IllegalArgumentException(("Unauthorized access to task"));
        }

        taskRepository.delete(task);
    }

    public Task getTaskById(long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Task"));

        User loggedInUser = getLoggedInUser();
        if (!task.getUser().getId().equals(loggedInUser.getId())
                && !loggedInUser.getRole().contains("ADMIN")) {
            throw new IllegalArgumentException(("Unauthorized access to task"));
        }
        return task;
    }

    public List<Task> getTasksByUser() {
        User loggedInUser = getLoggedInUser();
        return taskRepository.findByUserId(loggedInUser.getId());
    }

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
