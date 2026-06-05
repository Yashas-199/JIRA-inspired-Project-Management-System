package com.yashwanth.pms.comment.service;

import com.yashwanth.pms.comment.domain.Comment;
import com.yashwanth.pms.comment.repository.CommentRepository;
import com.yashwanth.pms.events.CommentAddedEvent;
import com.yashwanth.pms.issue.domain.Issue;
import com.yashwanth.pms.issue.service.IssueService;
import com.yashwanth.pms.task.domain.Task;
import com.yashwanth.pms.task.service.TaskService;
import com.yashwanth.pms.user.domain.User;
import com.yashwanth.pms.user.domain.Role;
import com.yashwanth.pms.user.service.UserService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final IssueService issueService;
    private final ApplicationEventPublisher publisher;

    public CommentServiceImpl(CommentRepository commentRepository, TaskService taskService, UserService userService, IssueService issueService, ApplicationEventPublisher publisher) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.userService = userService;
        this.issueService = issueService;
        this.publisher = publisher;
    }

    @Override
    public void deleteComment(UUID commentId, UUID currentUserId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new com.yashwanth.pms.common.exception.ResourceNotFoundException("Comment not found"));

        User currentUser = userService.getById(currentUserId);

        // allow if author or admin
        if (comment.getAuthor() != null && comment.getAuthor().getId().equals(currentUserId)) {
            commentRepository.delete(comment);
            return;
        }

        if (currentUser.getRole() == com.yashwanth.pms.user.domain.Role.ADMIN) {
            commentRepository.delete(comment);
            return;
        }

        throw new com.yashwanth.pms.common.exception.AccessDeniedException("You are not allowed to delete this comment");
    }

    @Override
    public Comment addTaskComment(UUID taskId, String content, UUID authorId, String visibilityStr) {

        Task task = taskService.getById(taskId);
        User author = userService.getById(authorId);

        com.yashwanth.pms.comment.domain.CommentVisibility visibility = com.yashwanth.pms.comment.domain.CommentVisibility.PROJECT;
        if (visibilityStr != null && !visibilityStr.isBlank()) {
            try {
                visibility = com.yashwanth.pms.comment.domain.CommentVisibility.valueOf(visibilityStr);
            } catch (IllegalArgumentException e) {
                // keep default
            }
        }

        Comment comment = Comment.forTaskWithVisibility(content, author, task, visibility);

        Comment savedComment = commentRepository.save(comment);

        List<UUID> recipients = new ArrayList<>();

        // Compute recipients based on visibility
        switch (visibility) {
            case PROJECT:
                if (task.getProject().getLeader() != null) recipients.add(task.getProject().getLeader().getId());
                if (task.getProject().getMembers() != null) task.getProject().getMembers().forEach(m -> recipients.add(m.getId()));
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                break;
            case ADMIN_MANAGER:
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                break;
            case MANAGER_LEADER:
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                if (task.getProject().getLeader() != null) recipients.add(task.getProject().getLeader().getId());
                break;
            case ADMIN_LEADER:
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                if (task.getProject().getLeader() != null) recipients.add(task.getProject().getLeader().getId());
                break;
            case PRIVATE:
                // only author
                recipients.add(author.getId());
                break;
        }

        // Deduplicate
        List<UUID> dedup = new ArrayList<>();
        for (UUID id : recipients) if (!dedup.contains(id)) dedup.add(id);

        publisher.publishEvent(new CommentAddedEvent(dedup, "New comment added to task: " + task.getTitle()));

        return savedComment;
    }


    @Override
    public Comment addIssueComment(UUID issueId, String content, UUID authorId, String visibilityStr) {
        Issue issue = issueService.getById(issueId);
        User author = userService.getById(authorId);

        com.yashwanth.pms.comment.domain.CommentVisibility visibility = com.yashwanth.pms.comment.domain.CommentVisibility.PROJECT;
        if (visibilityStr != null && !visibilityStr.isBlank()) {
            try {
                visibility = com.yashwanth.pms.comment.domain.CommentVisibility.valueOf(visibilityStr);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        Comment comment = Comment.forIssueWithVisibility(content, author, issue, visibility);

        Comment savedComment = commentRepository.save(comment);

        List<UUID> recipients = new ArrayList<>();

        switch (visibility) {
            case PROJECT:
                if (issue.getProject().getLeader() != null) recipients.add(issue.getProject().getLeader().getId());
                if (issue.getProject().getMembers() != null) issue.getProject().getMembers().forEach(m -> recipients.add(m.getId()));
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                break;
            case ADMIN_MANAGER:
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                break;
            case MANAGER_LEADER:
                userService.getUsersByRole(Role.PROJECT_MANAGER).forEach(pm -> recipients.add(pm.getId()));
                if (issue.getProject().getLeader() != null) recipients.add(issue.getProject().getLeader().getId());
                break;
            case ADMIN_LEADER:
                userService.getUsersByRole(Role.ADMIN).forEach(a -> recipients.add(a.getId()));
                if (issue.getProject().getLeader() != null) recipients.add(issue.getProject().getLeader().getId());
                break;
            case PRIVATE:
                recipients.add(author.getId());
                break;
        }

        List<UUID> dedup = new ArrayList<>();
        for (UUID id : recipients) if (!dedup.contains(id)) dedup.add(id);

        publisher.publishEvent(new CommentAddedEvent(dedup, "New comment added to issue: " + issue.getTitle()));

        return savedComment;
    }

    @Override
    public List<Comment> getCommentsForTask(UUID taskId, UUID viewerId) {

        Task task = taskService.getById(taskId);

        User viewer = userService.getById(viewerId);

        List<Comment> all = commentRepository.findByTaskOrderByCreatedAtAsc(task);

        List<Comment> visible = new ArrayList<>();
        for (Comment c : all) {
            if (canView(c, viewer, task.getProject())) visible.add(c);
        }
        return visible;
    }

    @Override
    public List<Comment> getCommentsForIssue(UUID issueId, UUID viewerId) {

        Issue issue = issueService.getById(issueId);

        User viewer = userService.getById(viewerId);

        List<Comment> all = commentRepository.findByIssueOrderByCreatedAtAsc(issue);

        List<Comment> visible = new ArrayList<>();
        for (Comment c : all) {
            if (canView(c, viewer, issue.getProject())) visible.add(c);
        }
        return visible;
    }

    private boolean canView(Comment comment, User viewer, com.yashwanth.pms.project.domain.Project project) {
        com.yashwanth.pms.comment.domain.CommentVisibility visibility = comment.getVisibility();
        // authors can always view
        if (comment.getAuthor().getId().equals(viewer.getId())) return true;

        // admins can view all comments
        if (viewer.getRole() == Role.ADMIN) return true;

        switch (visibility) {
            case PROJECT:
                if (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.PROJECT_MANAGER) return true;
                if (project.getLeader() != null && project.getLeader().getId().equals(viewer.getId())) return true;
                if (project.getMembers() != null) {
                    for (var m : project.getMembers()) if (m.getId().equals(viewer.getId())) return true;
                }
                return false;
            case ADMIN_MANAGER:
                return viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.PROJECT_MANAGER;
            case MANAGER_LEADER:
                return viewer.getRole() == Role.PROJECT_MANAGER || viewer.getRole() == Role.PROJECT_LEADER;
            case ADMIN_LEADER:
                // normally visible to Admin and Project Leader
                if (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.PROJECT_LEADER) return true;
                // allow Project Manager to view Admin-authored ADMIN_LEADER comments
                if (comment.getAuthor() != null && comment.getAuthor().getRole() == Role.ADMIN
                        && viewer.getRole() == Role.PROJECT_MANAGER) return true;
                return false;
            case PRIVATE:
                return false; // only author handled above
            default:
                return false;
        }
    }
}
