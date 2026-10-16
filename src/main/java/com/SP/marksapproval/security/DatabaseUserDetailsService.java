
package com.SP.marksapproval.security;

import com.SP.marksapproval.entity.Admin;
import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.repository.AdminRepository;
import com.SP.marksapproval.repository.FacultyRepository;
import com.SP.marksapproval.repository.StudentRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService, UserDetailsPasswordService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AdminRepository adminRepository;

    public DatabaseUserDetailsService(
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        String[] parts = loginId.split(":", 2);

        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid login ID");
        }

        String role = parts[0].toLowerCase();
        String identifier = parts[1];

        return switch (role) {
            case "student" -> {
                Student student = studentRepository.findByRollNo(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));

                yield User.withUsername(loginId)
                    .password(student.getPassword())
                    .roles("STUDENT")
                    .build();
            }
            case "faculty" -> {
                Faculty faculty = facultyRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));

                yield User.withUsername(loginId)
                    .password(faculty.getPassword())
                    .roles("FACULTY")
                    .build();
            }
            case "admin" -> {
                Admin admin = adminRepository.findByUsername(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));

                yield User.withUsername(loginId)
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
            }
            default -> throw new UsernameNotFoundException("Invalid role");
        };
    }

    @Override
    public UserDetails updatePassword(
            UserDetails user, String newPassword) {

        String[] parts = user.getUsername().split(":", 2);
        String role = parts[0].toLowerCase();
        String identifier = parts[1];

        switch (role) {
            case "student" -> {
                Student student = studentRepository.findByRollNo(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));
                student.setPassword(newPassword);
                studentRepository.save(student);
            }
            case "faculty" -> {
                Faculty faculty = facultyRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));
                faculty.setPassword(newPassword);
                facultyRepository.save(faculty);
            }
            case "admin" -> {
                Admin admin = adminRepository.findByUsername(identifier)
                    .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));
                admin.setPassword(newPassword);
                adminRepository.save(admin);
            }
            default -> throw new UsernameNotFoundException("Invalid role");
        }

        return User.withUserDetails(user)
            .password(newPassword)
            .build();
    }
}
