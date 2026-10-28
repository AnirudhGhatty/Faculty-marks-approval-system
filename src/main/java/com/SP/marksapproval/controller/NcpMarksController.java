
package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.service.FacultyService;
import com.SP.marksapproval.service.FacultySubjectService;
import com.SP.marksapproval.service.NcpMarksService;
import com.SP.marksapproval.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ncp-marks")
public class NcpMarksController {

    private final NcpMarksService ncpMarksService;
    private final FacultyService facultyService;
    private final FacultySubjectService facultySubjectService;
    private final StudentService studentService;

    public NcpMarksController(
            NcpMarksService ncpMarksService,
            FacultyService facultyService,
            FacultySubjectService facultySubjectService,
            StudentService studentService) {

        this.ncpMarksService = ncpMarksService;
        this.facultyService = facultyService;
        this.facultySubjectService = facultySubjectService;
        this.studentService = studentService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitNcpMarks(
            @RequestParam Integer assignmentId,
            @RequestParam Integer studentId,
            @RequestParam String ncp,
            @RequestParam Integer score,
            Principal principal) {

        // Verify that the request comes from an authenticated faculty account.
        if (principal == null
                || principal.getName() == null
                || !principal.getName().startsWith("faculty:")) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in with a faculty account.");
        }

        String email = principal.getName().substring("faculty:".length());

        Optional<Faculty> facultyOptional =
                facultyService.getFacultyByEmail(email);

        if (facultyOptional.isEmpty()) {
            return error(
                    HttpStatus.FORBIDDEN,
                    "Faculty account could not be verified.");
        }

        Faculty faculty = facultyOptional.get();

        // Find the requested assignment.
        Optional<FacultySubject> assignmentOptional =
                facultySubjectService.getAssignmentById(assignmentId);

        if (assignmentOptional.isEmpty()) {
            return error(
                    HttpStatus.NOT_FOUND,
                    "The requested assignment does not exist.");
        }

        FacultySubject assignment = assignmentOptional.get();

        // Verify that this assignment belongs to the logged-in faculty member.
        if (assignment.getFaculty() == null
                || !assignment.getFaculty().getFacultyId()
                        .equals(faculty.getFacultyId())) {

            return error(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to submit marks for this assignment.");
        }

        // Confirm that the student belongs to this assignment's roster.
        List<Student> roster =
                studentService.getStudentsForSubjectSection(
                        assignment.getSubject(),
                        assignment.getSection());

        boolean studentBelongsToAssignment = roster.stream()
                .anyMatch(student ->
                        student.getStudentId().equals(studentId));

        if (!studentBelongsToAssignment) {
            return error(
                    HttpStatus.FORBIDDEN,
                    "This student does not belong to the assigned subject and section.");
        }

        // Validate the score and lock only the selected NCP.
        try {
            ncpMarksService.submitNcpMarks(
                    roster.stream()
                            .filter(student ->
                                    student.getStudentId().equals(studentId))
                            .findFirst()
                            .orElseThrow(),
                    assignment.getSubject(),
                    ncp,
                    score);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            ncp.toUpperCase()
                                    + " marks submitted and locked successfully."));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return error(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message) {

        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }
}
