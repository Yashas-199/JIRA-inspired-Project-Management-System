package com.yashwanth.pms.project.controller;

import com.yashwanth.pms.common.exception.AccessDeniedException;
import com.yashwanth.pms.project.domain.Project;
import com.yashwanth.pms.project.dto.CreateProjectRequest;
import com.yashwanth.pms.project.dto.ProjectMemberRequest;
import com.yashwanth.pms.project.dto.ProjectResponse;
import com.yashwanth.pms.project.service.ProjectService;
import com.yashwanth.pms.security.UserPrincipal;
import com.yashwanth.pms.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping()
    // allow any authenticated user to fetch projects visible to them
    public List<ProjectResponse> getAllProjects(Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return projectService.getProjectsForUser(principal.getId()).stream()
                .map(ProjectResponse::from)
                .toList();

    }

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    // allow any authenticated user to create a project; service layer will enforce rules
    public ProjectResponse createProject(
        @Valid @RequestBody CreateProjectRequest request, Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Project project = projectService.createProject(
                request.getName(),
                request.getLeaderId(),
                principal.getId()
        );
        return ProjectResponse.from(project);
    }

    @PostMapping("/{projectId}/members")
    // allow ADMIN, PROJECT_MANAGER or the PROJECT_LEADER of the project to add members
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_LEADER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(@PathVariable UUID projectId, @Valid @RequestBody ProjectMemberRequest request, Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        projectService.addMember(projectId, request.getUserId(), userPrincipal.getId());
    }

    @DeleteMapping("/{projectId}/members/{userId}")
   @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_LEADER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        projectService.removeMember(projectId, userId, principal.getId());
    }

    @GetMapping("/{projectId}")
    // allow any authenticated user to view project - service layer / checks will enforce visibility
    public ProjectResponse getProject(@PathVariable UUID projectId, Authentication authentication) {

        Project project = projectService.getById(projectId);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Project managers and admins may view any project. Project leaders may view projects they lead.
        if (principal.getRole().equals("PROJECT_LEADER") && !project.getLeader().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You are not allowed to access this project");
        }

        if (principal.getRole().equals("TEAM_MEMBER") && (project.getMembers() == null || project.getMembers().stream().noneMatch(m -> m.getId().equals(principal.getId())))) {
            throw new AccessDeniedException("You are not allowed to access this project");
        }

        return ProjectResponse.from(project);

    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')") 
    public ProjectResponse updateProject(@PathVariable UUID projectId, @RequestBody Map<String, String> body, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Project updated = projectService.updateProject(projectId, body.get("name"), body.get("status"), principal.getId());

        return ProjectResponse.from(updated);
    }

    @DeleteMapping("/{projectId}")
@PreAuthorize("hasRole('ADMIN')")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteProject(@PathVariable UUID projectId, Authentication authentication) {
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    projectService.deleteProject(projectId, principal.getId());
}
}
