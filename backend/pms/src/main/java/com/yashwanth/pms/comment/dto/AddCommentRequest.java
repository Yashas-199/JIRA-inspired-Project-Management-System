package com.yashwanth.pms.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class AddCommentRequest {

    @NotBlank
    private String content;

    // optional visibility: PROJECT, ADMIN_MANAGER, MANAGER_LEADER, ADMIN_LEADER, PRIVATE
    private String visibility;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
