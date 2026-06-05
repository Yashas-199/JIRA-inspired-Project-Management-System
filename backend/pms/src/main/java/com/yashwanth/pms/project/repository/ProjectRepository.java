package com.yashwanth.pms.project.repository;

import com.yashwanth.pms.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

	// Projects where the supplied user is the leader
	List<Project> findByLeaderId(UUID leaderId);

	// Projects where the supplied user is a member
	List<Project> findByMembers_Id(UUID memberId);

}
