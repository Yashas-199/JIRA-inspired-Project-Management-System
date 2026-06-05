package com.yashwanth.pms.issue.service;

import com.yashwanth.pms.common.exception.AccessDeniedException;
import com.yashwanth.pms.common.exception.ResourceNotFoundException;
import com.yashwanth.pms.events.IssueAssignedEvent;
import com.yashwanth.pms.events.IssueStatusChangedEvent;
import com.yashwanth.pms.issue.domain.Issue;
import com.yashwanth.pms.issue.domain.IssuePriority;
import com.yashwanth.pms.issue.domain.IssueStatus;
import com.yashwanth.pms.issue.domain.IssueType;
import com.yashwanth.pms.issue.repository.IssueRepository;
import com.yashwanth.pms.comment.repository.CommentRepository;
import com.yashwanth.pms.project.domain.Project;
import com.yashwanth.pms.project.service.ProjectService;
import com.yashwanth.pms.task.domain.Task;
import com.yashwanth.pms.task.service.TaskService;
import com.yashwanth.pms.user.domain.Role;
import com.yashwanth.pms.user.domain.User;
import com.yashwanth.pms.user.service.UserService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;

    public IssueServiceImpl(IssueRepository issueRepository, CommentRepository commentRepository, ProjectService projectService, TaskService taskService, UserService userService, ApplicationEventPublisher publisher) {
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.projectService = projectService;
        this.taskService = taskService;
        this.userService = userService;
        this.publisher = publisher;
    }

    @Override
    public Issue createIssue(UUID projectId, UUID taskId, String title, String description, String type, String priority, UUID reporterId) {

        User reporter = userService.getById(reporterId);
        // Admins should not be allowed to create/raise issues
        if (reporter.getRole() == com.yashwanth.pms.user.domain.Role.ADMIN) {
            throw new com.yashwanth.pms.common.exception.AccessDeniedException("Admin cannot raise issues");
        }
        Project project = projectService.getById(projectId);
        Task task = (taskId != null) ? taskService.getById(taskId) : null;

        Issue issue = new Issue(title, description, IssueType.valueOf(type), IssuePriority.valueOf(priority), project, task, reporter);

        System.out.println("Issue created successfully...");

        return issueRepository.save(issue);
    }

    @Override
    public void assignIssue(UUID issueId, UUID assigneeId, UUID currentUserId) {
        User currentUser = userService.getById(currentUserId);

        if(currentUser.getRole() != Role.PROJECT_LEADER) {
            throw new AccessDeniedException("Not allowed to assign issues");
        }

        Issue issue = getById(issueId);

        User assignee = userService.getById(assigneeId);

        issue.assignTo(assignee);
        issueRepository.save(issue);

        publisher.publishEvent(new IssueAssignedEvent(assigneeId, issueId, issue.getProject().getId(), issue.getTitle()));
    }

    @Override
public void changeIssueStatus(UUID issueId, String newStatus, UUID currentUserId) {

    User currentUser = userService.getById(currentUserId);
    Issue issue = getById(issueId);
    IssueStatus target = IssueStatus.valueOf(newStatus);

    // Project Leader can only set OPEN, IN_PROGRESS, RESOLVED
    if (currentUser.getRole() == Role.PROJECT_LEADER) {
        if (target == IssueStatus.CLOSED) {
            throw new AccessDeniedException("Project Leader cannot close issues. Escalate to Project Manager.");
        }
    }

    // Project Manager can set any status (IN_PROGRESS, RESOLVED, CLOSED)
    // No extra restriction needed for PROJECT_MANAGER

    IssueStatus currentStatus = issue.getStatus();
    issue.changeStatus(target);
    issueRepository.save(issue);

    publisher.publishEvent(new IssueStatusChangedEvent(
        issueId,
        issue.getReporter().getId(),
        currentStatus,
        target,
        issue.getAssignee() != null ? issue.getAssignee().getName() : "Unassigned",
        issue.getTitle()
    ));
}
    @Override
    public List<Issue> getIssuesByProject(UUID projectId) {
        Project project = projectService.getById(projectId);

        return issueRepository.findByProject(project);
    }

    @Override
    public Issue getById(UUID issueId) {
        return issueRepository.findById(issueId).orElseThrow(() -> new ResourceNotFoundException("Issue does not exist"));
    }

    @Override
    public void deleteIssue(UUID issueId, UUID currentUserId) {
        Issue issue = getById(issueId);
        User currentUser = userService.getById(currentUserId);

        // Admin can always delete
        if (currentUser.getRole() == com.yashwanth.pms.user.domain.Role.ADMIN) {
            issueRepository.delete(issue);
            return;
        }

        // Project Manager can delete any issue
        if (currentUser.getRole() == com.yashwanth.pms.user.domain.Role.PROJECT_MANAGER) {
            issueRepository.delete(issue);
            return;
        }

        // Reporter may delete only if issue is OPEN, unassigned and has no comments
        if (issue.getReporter() != null && issue.getReporter().getId().equals(currentUserId)) {
            boolean unassigned = issue.getAssignee() == null;
            boolean open = issue.getStatus() == com.yashwanth.pms.issue.domain.IssueStatus.OPEN;
            int commentsCount = 0;
            try {
                commentsCount = commentRepository.findByIssueOrderByCreatedAtAsc(issue).size();
            } catch (Exception ignored) {}

            if (unassigned && open && commentsCount == 0) {
                issueRepository.delete(issue);
                return;
            }
        }

        throw new com.yashwanth.pms.common.exception.AccessDeniedException("You are not allowed to delete this issue");
    }
}
