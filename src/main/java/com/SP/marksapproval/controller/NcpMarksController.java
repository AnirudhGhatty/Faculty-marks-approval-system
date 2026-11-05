
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
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Bulk submission of one NCP for an entire assigned roster.
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitNcpMarks(
            @RequestParam Integer assignmentId,
            @RequestParam String ncp,
            @RequestParam Map<String, String> requestParams,
            Principal principal) {

        // 1. Verify faculty authentication.
        if (principal == null
                || principal.getName() == null
                || !principal.getName().startsWith("faculty:")) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in with a faculty account.");
        }

        String email = principal.getName()
                .substring("faculty:".length());

        Optional<Faculty> facultyOptional =
                facultyService.getFacultyByEmail(email);

        if (facultyOptional.isEmpty()) {
            return error(
                    HttpStatus.FORBIDDEN,
                    "Faculty account could not be verified.");
        }

        Faculty faculty = facultyOptional.get();

        // 2. Verify assignment.
        Optional<FacultySubject> assignmentOptional =
                facultySubjectService.getAssignmentById(assignmentId);

        if (assignmentOptional.isEmpty()) {
            return error(
                    HttpStatus.NOT_FOUND,
                    "The requested assignment does not exist.");
        }

        FacultySubject assignment = assignmentOptional.get();

        if (assignment.getFaculty() == null
                || !assignment.getFaculty().getFacultyId()
                        .equals(faculty.getFacultyId())) {

            return error(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to submit marks for this assignment.");
        }

        // 3. Fetch the exact cohort and section roster.
        // Class Of now belongs to Subject, not FacultySubject.
        List<Student> roster =
                studentService.getStudentsForSubjectSection(
                        assignment.getSubject(),
                        assignment.getSubject().getClassOf(),
                        assignment.getSection());

        if (roster.isEmpty()) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    "There are no students in this assignment's roster.");
        }

        // 4. Extract submitted student scores.
        Map<Integer, Integer> scores = new HashMap<>();

        Set<Integer> rosterIds = roster.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        for (Map.Entry<String, String> entry
                : requestParams.entrySet()) {

            String key = entry.getKey();

            if (!key.startsWith("scores[")) {
                continue;
            }

            try {
                int closingBracket = key.indexOf(']');

                if (closingBracket < 0) {
                    return error(
                            HttpStatus.BAD_REQUEST,
                            "Invalid student score field.");
                }

                Integer studentId = Integer.parseInt(
                        key.substring(7, closingBracket));

                if (!rosterIds.contains(studentId)) {
                    return error(
                            HttpStatus.FORBIDDEN,
                            "A submitted student does not belong to this assignment.");
                }

                Integer score = Integer.valueOf(entry.getValue());

                if (scores.put(studentId, score) != null) {
                    return error(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate score submitted for a student.");
                }

            } catch (NumberFormatException ex) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Student IDs and marks must be valid whole numbers.");
            }
        }

        // 5. Require a score for every student.
        if (scores.size() != roster.size()) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    "Please enter marks for every student before submitting.");
        }

        // 6. Submit the entire batch.
        try {
            ncpMarksService.submitBulkNcpMarks(
                    roster,
                    assignment.getSubject(),
                    ncp,
                    scores);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            ncp.toUpperCase()
                                    + " marks submitted and locked successfully for "
                                    + roster.size() + " students."));

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
