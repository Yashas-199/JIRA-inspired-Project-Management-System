package com.yashwanth.pms.comment.domain;

public enum CommentVisibility {
    PROJECT,        // visible to project leader, members, project manager, admins
    ADMIN_MANAGER,  // visible only to admins and project managers
    MANAGER_LEADER, // visible only to project managers and project leader
    ADMIN_LEADER,   // visible only to admins and project leader
    PRIVATE         // visible only to the author
}
