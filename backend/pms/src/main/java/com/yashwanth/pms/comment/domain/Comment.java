package com.yashwanth.pms.comment.domain;

import com.yashwanth.pms.issue.domain.Issue;
import com.yashwanth.pms.task.domain.Task;
import com.yashwanth.pms.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import com.yashwanth.pms.comment.domain.CommentVisibility;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1500)
    private String content;

    @ManyToOne(optional = false)
    private User author;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Issue issue;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentVisibility visibility;

    protected Comment() {
        // for JPA
    }

    private Comment(String content,
                    User author,
                    Task task,
                    Issue issue,
                    CommentVisibility visibility) {

        this.content = content;
        this.author = author;
        this.task = task;
        this.issue = issue;
        this.visibility = visibility == null ? CommentVisibility.PROJECT : visibility;
        this.createdAt = LocalDateTime.now();
    }

    /* =========================
       Factory methods
       ========================= */

    public static Comment forTask(
            String content,
            User author,
            Task task
    ) {
        return new Comment(content, author, task, null, CommentVisibility.PROJECT);
    }

    public static Comment forIssue(
            String content,
            User author,
            Issue issue
    ) {
        return new Comment(content, author, null, issue, CommentVisibility.PROJECT);
    }

    public static Comment forTaskWithVisibility(String content, User author, Task task, CommentVisibility visibility) {
        return new Comment(content, author, task, null, visibility == null ? CommentVisibility.PROJECT : visibility);
    }

    public static Comment forIssueWithVisibility(String content, User author, Issue issue, CommentVisibility visibility) {
        return new Comment(content, author, null, issue, visibility == null ? CommentVisibility.PROJECT : visibility);
    }

    /* =========================
       Getters
       ========================= */

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }

    public Task getTask() {
        return task;
    }

    public Issue getIssue() {
        return issue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CommentVisibility getVisibility() {
        return visibility;
    }
}
