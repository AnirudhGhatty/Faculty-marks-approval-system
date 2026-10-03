package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
}