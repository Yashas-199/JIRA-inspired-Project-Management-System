package com.yashwanth.pms.comment.service;

import com.yashwanth.pms.comment.domain.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    Comment addTaskComment(UUID taskId, String content, UUID authorId, String visibility);

    Comment addIssueComment(UUID issueId, String content, UUID authorId, String visibility);

    List<Comment> getCommentsForTask(UUID taskId, UUID viewerId);

    List<Comment> getCommentsForIssue(UUID issueId, UUID viewerId);

    void deleteComment(UUID commentId, UUID currentUserId);

}
