package com.yashwanth.pms.project.service;

import com.yashwanth.pms.common.exception.AccessDeniedException;
import com.yashwanth.pms.common.exception.BusinessException;
import com.yashwanth.pms.common.exception.ResourceNotFoundException;
import com.yashwanth.pms.events.ProjectMemberAddedEvent;
import com.yashwanth.pms.project.domain.Project;
import com.yashwanth.pms.project.domain.ProjectStatus;
import com.yashwanth.pms.project.repository.ProjectRepository;
import com.yashwanth.pms.user.domain.Role;
import com.yashwanth.pms.user.domain.User;
import com.yashwanth.pms.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserService userService, ApplicationEventPublisher publisher) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.publisher = publisher;
    }

    @Override
    public Project createProject(String name, UUID leaderId, UUID currentUserId) {
        // Allow admins to create projects for any leader. Non-admin users may create a project
        // only if they are creating it for themselves (i.e., leaderId == currentUserId).

        User currentUser = userService.getById(currentUserId);

        if (currentUser.getRole() != Role.ADMIN && !currentUserId.equals(leaderId)) {
            throw new AccessDeniedException("Only admin can create projects for other users");
        }

        User leader = userService.getById(leaderId);

        // If the chosen leader is not already a project leader, promote them.
        if (leader.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot assign Admin as a project leader");
        }

        if (leader.getRole() == Role.PROJECT_MANAGER) {
            throw new BusinessException("User is a Project Manager, cannot be assigned as leader");
        }

        // promote to PROJECT_LEADER only when not already a project leader
        if (leader.getRole() != Role.PROJECT_LEADER) {
            leader.setRole(Role.PROJECT_LEADER);
            userService.save(leader);
        }
        Project project = new Project(name, leader);
        return projectRepository.save(project);
    }


    @Override
    public void addMember(UUID projectId, UUID userId, UUID currentUserId) {


        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if(project.getStatus() != ProjectStatus.ACTIVE) {
            throw new BusinessException("Project is not active");
        }

        User currentUser = userService.getById(currentUserId);

        authorize(project, currentUser);

        User userToAdd = userService.getById(userId);

        if (project.getMembers().contains(userToAdd)) {
            throw new BusinessException("User already a member");
        }

        project.getMembers().add(userToAdd);
        projectRepository.save(project);

        List<UUID> members = new ArrayList<>();

        project.getMembers().forEach(m -> members.add(m.getId()));

        publisher.publishEvent(new ProjectMemberAddedEvent(project.getName(), userToAdd.getName(), userToAdd.getEmail(), members));

    }

    @Override
    public void removeMember(UUID projectId, UUID userId, UUID currentUserId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if(project.getStatus() != ProjectStatus.ACTIVE) {
            throw new BusinessException("Project is not active");
        }

        User currentUser = userService.getById(currentUserId);

        authorize(project, currentUser);

        User userToRemove = userService.getById(userId);

        if (userToRemove.equals(project.getLeader())) {
            throw new BusinessException("Cannot remove project leader");
        }

        project.getMembers().remove(userToRemove);
        projectRepository.save(project);
    }

    @Override
    public Project getById(UUID projectId) {

        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void authorize(Project project, User currentUser) {
    if (currentUser.getRole() != Role.ADMIN &&
            currentUser.getRole() != Role.PROJECT_MANAGER &&
            !currentUser.equals(project.getLeader())) {
        throw new AccessDeniedException("Not authorized");
    }
}

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public List<Project> getProjectsForUser(UUID userId) {
        User user = userService.getById(userId);

        // Admins and Project Managers should see all projects
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROJECT_MANAGER) {
            return projectRepository.findAll();
        }

        List<Project> result = new ArrayList<>();

        // projects where user is leader
        List<Project> leaderProjects = projectRepository.findByLeaderId(userId);
        if (leaderProjects != null && !leaderProjects.isEmpty()) {
            result.addAll(leaderProjects);
        }

        // projects where user is member
        List<Project> memberProjects = projectRepository.findByMembers_Id(userId);
        if (memberProjects != null && !memberProjects.isEmpty()) {
            for (Project p : memberProjects) {
                if (!result.contains(p)) result.add(p);
            }
        }

        return result;
    }

    @Override
    public Project updateProject(UUID projectId, String name, String status, UUID currentUserId) {
        Project project = getById(projectId);

        User currentUser = userService.getById(currentUserId);

        // only admins and project leader may update project
        authorize(project, currentUser);

        boolean changed = false;

        if (name != null && !name.isBlank()) {
            project.setName(name);
            changed = true;
        }

        if (status != null && !status.isBlank()) {
            ProjectStatus newStatus = ProjectStatus.valueOf(status);
            if (project.getStatus() != newStatus) {
                project.setStatus(newStatus);
                changed = true;
            }
        }

        if (changed) {
            projectRepository.save(project);
        }

        return project;
    }

    @Override
public void deleteProject(UUID projectId, UUID currentUserId) {
    Project project = getById(projectId);

    User currentUser = userService.getById(currentUserId);

    if (currentUser.getRole() != Role.ADMIN) {
        throw new AccessDeniedException("Only admin can delete projects");
    }

    projectRepository.delete(project);
}
}
