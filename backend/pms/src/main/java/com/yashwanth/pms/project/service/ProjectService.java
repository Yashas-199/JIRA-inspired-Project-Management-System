package com.yashwanth.pms.project.service;

import com.yashwanth.pms.project.domain.Project;
import com.yashwanth.pms.user.domain.User;

import java.util.List;
import java.util.UUID;


public interface ProjectService {

    Project createProject(String name, UUID leaderId, UUID currentUserId);

    void addMember(UUID projectId, UUID userId, UUID currentUserId);

    void removeMember(UUID projectId, UUID userId, UUID currentUserId);
    void deleteProject(UUID projectId, UUID currentUserId);

    Project getById(UUID projectId);

    List<Project> getAllProjects();

    /**
     * Get projects visible to a specific user. Admins see all projects, leaders and members see
     * projects where they are leader or member.
     */
    List<Project> getProjectsForUser(UUID userId);

    Project updateProject(UUID projectId, String name, String status, UUID currentUserId);
}
