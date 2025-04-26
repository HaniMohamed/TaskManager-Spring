package com.taskmanager.service;


import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Task createTask(TaskDTO taskDTO) {

        Task newTask = new Task(taskDTO.title(), taskDTO.description(), taskDTO.status(), getUser());
        return taskRepository.save(newTask);
    }

    public Task updateTask(TaskDTO taskDTO, long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        if (!task.getUser().getUsername().equals(username) && !role.contains("ROLE_ADMIN")) {
            throw new IllegalArgumentException("Unauthorized access to task");
        }

        task.setTitle(taskDTO.title());
        task.setStatus(taskDTO.status());
        task.setDescription(taskDTO.description());
        return taskRepository.save(task);

    }

    private User getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
