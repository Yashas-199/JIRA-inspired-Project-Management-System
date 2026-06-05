package com.yashwanth.pms.issue.controller;

import com.yashwanth.pms.issue.dto.CreateIssueRequest;
import com.yashwanth.pms.issue.dto.IssueResponse;
import com.yashwanth.pms.issue.service.IssueService;
import com.yashwanth.pms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Allow project managers, project leaders and team members to raise issues
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'PROJECT_LEADER', 'TEAM_MEMBER')")
    public IssueResponse createIssue(@PathVariable UUID projectId, @Valid @RequestBody CreateIssueRequest request, Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        System.out.println("In issue controller...");

        return IssueResponse.from(
                issueService.createIssue(
                        projectId, request.getTaskId(), request.getTitle(), request.getDescription(), request.getType(), request.getPriority(), principal.getId())
        );

    }

    @PostMapping("/{issueId}/assign/{userId}")
    // Allow managers, leaders and admins to assign issues
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'PROJECT_LEADER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignIssue(@PathVariable UUID issueId, @PathVariable UUID userId, Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        issueService.assignIssue(issueId, userId, principal.getId());
    }

    @PatchMapping("/{issueId}/status")
    // Only project managers and admins can change issue status (project leaders shouldn't close issues)
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeIssueStatus(@PathVariable UUID issueId, @RequestBody Map<String, String> body, Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        issueService.changeIssueStatus(issueId, body.get("status"), principal.getId());

    }

    @GetMapping
    public List<IssueResponse> getIssues(
            @PathVariable UUID projectId) {

        return issueService.getIssuesByProject(projectId)
                .stream()
                .map(IssueResponse::from)
                .toList();
    }

}
