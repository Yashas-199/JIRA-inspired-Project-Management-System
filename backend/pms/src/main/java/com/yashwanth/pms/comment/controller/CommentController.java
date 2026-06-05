package com.yashwanth.pms.comment.controller;

import com.yashwanth.pms.comment.dto.AddCommentRequest;
import com.yashwanth.pms.comment.dto.CommentResponse;
import com.yashwanth.pms.comment.service.CommentService;
import com.yashwanth.pms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{taskId}/comments")
    // Allow admins, project managers, leaders and team members to comment on tasks
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_LEADER', 'TEAM_MEMBER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addCommentTask(@PathVariable UUID taskId, @RequestBody @Valid AddCommentRequest request, Authentication authentication) {

    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

    return CommentResponse.from(
        commentService.addTaskComment(taskId, request.getContent(), principal.getId(), request.getVisibility())
    );
     }

     @GetMapping("/tasks/{taskId}/comments")
     public List<CommentResponse> getTaskComments(@PathVariable UUID taskId, Authentication authentication) {
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

    return commentService.getCommentsForTask(taskId, principal.getId())
        .stream()
        .map(CommentResponse::from)
        .collect(Collectors.toList());
     }


    @PostMapping("/issues/{issueId}/comments")
    // Allow admins, project managers, leaders and team members to comment on issues
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_LEADER', 'TEAM_MEMBER')")
     @ResponseStatus(HttpStatus.CREATED)
     public CommentResponse addCommentIssue(@PathVariable UUID issueId, @RequestBody @Valid AddCommentRequest request, Authentication authentication) {

         UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

         return CommentResponse.from(
                 commentService.addIssueComment(issueId, request.getContent(), principal.getId(), request.getVisibility())
         );
     }

     @GetMapping("/issues/{issueId}/comments")
     public List<CommentResponse> getIssueComments(@PathVariable UUID issueId, Authentication authentication) {
         UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

         return commentService.getCommentsForIssue(issueId, principal.getId())
                 .stream()
                 .map(CommentResponse::from)
                 .collect(Collectors.toList());
     }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Allow authenticated users; service will enforce author/admin rule
    public void deleteComment(@PathVariable UUID commentId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        commentService.deleteComment(commentId, principal.getId());
    }


}
