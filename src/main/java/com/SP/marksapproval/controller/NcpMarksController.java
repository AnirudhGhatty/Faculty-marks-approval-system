
package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.entity.Student;

import com.SP.marksapproval.service.ExcelMarksService;
import com.SP.marksapproval.service.FacultyService;
import com.SP.marksapproval.service.FacultySubjectService;
import com.SP.marksapproval.service.NcpMarksService;
import com.SP.marksapproval.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final ExcelMarksService excelMarksService;

    public NcpMarksController(
            NcpMarksService ncpMarksService,
            FacultyService facultyService,
            FacultySubjectService facultySubjectService,
            StudentService studentService,
            ExcelMarksService excelMarksService) {

        this.ncpMarksService = ncpMarksService;
        this.facultyService = facultyService;
        this.facultySubjectService = facultySubjectService;
        this.studentService = studentService;
        this.excelMarksService = excelMarksService;
    }

    /**
     * Saves an incomplete NCP marks draft without locking it.
     */
    @PostMapping("/save-draft")
    public ResponseEntity<Map<String, String>> saveNcpMarksDraft(
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

        // 2. Verify assignment ownership.
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
                    "You are not authorized to save marks for this assignment.");
        }

        // 3. Fetch the exact cohort and section roster.
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

        // 4. Extract scores. Blank fields are allowed in drafts.
        Map<Integer, BigDecimal> scores = new HashMap<>();

        Set<Integer> rosterIds = roster.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        for (Map.Entry<String, String> entry
                : requestParams.entrySet()) {

            String key = entry.getKey();

            if (!key.startsWith("scores[")) {
                continue;
            }

            int closingBracket = key.indexOf(']');

            if (closingBracket < 0) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Invalid student score field.");
            }

            Integer studentId;

            try {
                studentId = Integer.parseInt(
                        key.substring(7, closingBracket));
            } catch (NumberFormatException ex) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Invalid student ID in submitted score field.");
            }

            if (!rosterIds.contains(studentId)) {
                return error(
                        HttpStatus.FORBIDDEN,
                        "A submitted student does not belong to this assignment.");
            }

            if (scores.containsKey(studentId)) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate score submitted for a student.");
            }

            String rawScore = entry.getValue();

            // Blank means no mark. It does not invalidate a draft.
            BigDecimal score = null;

            if (rawScore != null && !rawScore.trim().isEmpty()) {
                try {
                    score = new BigDecimal(rawScore.trim());
                } catch (NumberFormatException ex) {
                    return error(
                            HttpStatus.BAD_REQUEST,
                            "Marks must be valid numeric values with at most 2 decimal places.");
                }
            }

            scores.put(studentId, score);
        }

        // 5. Save the draft without locking the assessment.
        try {
            ncpMarksService.saveDraftBulkNcpMarks(
                    roster,
                    assignment.getSubject(),
                    ncp,
                    scores);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            ncp.toUpperCase()
                                    + " draft saved successfully. You can continue editing it later."));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return error(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
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

        // 2. Verify assignment ownership.
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
        // Class Of belongs to Subject, not FacultySubject.
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
        Map<Integer, BigDecimal> scores = new HashMap<>();

        Set<Integer> rosterIds = roster.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        for (Map.Entry<String, String> entry
                : requestParams.entrySet()) {

            String key = entry.getKey();

            if (!key.startsWith("scores[")) {
                continue;
            }

            int closingBracket = key.indexOf(']');

            if (closingBracket < 0) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Invalid student score field.");
            }

            Integer studentId;

            try {
                studentId = Integer.parseInt(
                        key.substring(7, closingBracket));
            } catch (NumberFormatException ex) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Invalid student ID in submitted score field.");
            }

            if (!rosterIds.contains(studentId)) {
                return error(
                        HttpStatus.FORBIDDEN,
                        "A submitted student does not belong to this assignment.");
            }

            String rawScore = entry.getValue();

            if (rawScore == null || rawScore.trim().isEmpty()) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Please enter marks for every student before submitting.");
            }

            BigDecimal score;

            try {
                score = new BigDecimal(rawScore.trim());
            } catch (NumberFormatException ex) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Marks must be valid numeric values with at most 2 decimal places.");
            }

            if (scores.putIfAbsent(studentId, score) != null) {
                return error(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate score submitted for a student.");
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

    /**
     * Uploads an Excel file and returns marks mapped to student IDs.
     * Does not save drafts or submit/lock marks.
     */
    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcelMarks(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer assignmentId,
            @RequestParam String ncp,
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

        // 2. Verify assignment ownership.
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
                    "You are not authorized to upload marks for this assignment.");
        }

        // 3. Validate the selected NCP.
        String selectedNcp = ncp.trim().toUpperCase();

        if (!Set.of("NCP1", "NCP2", "NCP3").contains(selectedNcp)) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid NCP selected.");
        }

        // 4. Fetch the exact cohort and section roster.
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

        // 5. Parse the Excel file. No database writes occur.
        try {
            Map<Integer, BigDecimal> importedMarks =
                    excelMarksService.parseMarks(
                            file,
                            roster,
                            selectedNcp);

            return ResponseEntity.ok(
                    Map.of("marks", importedMarks));

        } catch (IllegalArgumentException ex) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message) {

        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }
}
