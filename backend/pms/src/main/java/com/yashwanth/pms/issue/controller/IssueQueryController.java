package com.yashwanth.pms.issue.controller;

import com.yashwanth.pms.issue.dto.IssueResponse;
import com.yashwanth.pms.issue.service.IssueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
public class IssueQueryController {

    private final IssueService issueService;

    public IssueQueryController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/{issueId}")
    public IssueResponse getIssueById(@PathVariable UUID issueId) {
        return IssueResponse.from(issueService.getById(issueId));
    }

    @DeleteMapping("/{issueId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteIssue(@PathVariable UUID issueId, org.springframework.security.core.Authentication authentication) {
        com.yashwanth.pms.security.UserPrincipal principal = (com.yashwanth.pms.security.UserPrincipal) authentication.getPrincipal();
        issueService.deleteIssue(issueId, principal.getId());
    }

}
