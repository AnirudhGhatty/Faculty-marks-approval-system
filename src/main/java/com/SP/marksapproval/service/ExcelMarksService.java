
package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.Student;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelMarksService {

    public Map<Integer, BigDecimal> parseMarks(
            MultipartFile file,
            List<Student> roster,
            String ncp) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an Excel file.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported.");
        }

        int maximumMarks = switch (ncp) {
            case "NCP1" -> 15;
            case "NCP2" -> 30;
            case "NCP3" -> 15;
            default -> throw new IllegalArgumentException("Invalid NCP selected.");
        };

        Map<String, Student> studentsByRollNo = new HashMap<>();

        for (Student student : roster) {
            studentsByRollNo.put(student.getRollNo().trim(), student);
        }

        Map<Integer, BigDecimal> importedMarks = new LinkedHashMap<>();
        Set<String> processedRollNumbers = new HashSet<>();

        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);

            if (header == null
                    || !"Enrolno".equalsIgnoreCase(
                            formatter.formatCellValue(header.getCell(1)).trim())
                    || !"Marks".equalsIgnoreCase(
                            formatter.formatCellValue(header.getCell(4)).trim())) {

                throw new IllegalArgumentException(
                        "Invalid Excel template. Expected Enrolno in column B "
                        + "and Marks in column E.");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String rollNo = formatter.formatCellValue(row.getCell(1)).trim();
                String marksText = formatter.formatCellValue(row.getCell(4)).trim();

                // Ignore completely empty rows.
                if (rollNo.isEmpty() && marksText.isEmpty()) {
                    continue;
                }

                if (rollNo.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Missing enrollment number at Excel row " + (i + 1));
                }

                // Find the student in the currently selected roster.
                Student student = studentsByRollNo.get(rollNo);

                // Ignore records belonging to students outside this roster.
                if (student == null) {
                    continue;
                }

                if (marksText.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Missing marks for " + rollNo
                            + " at Excel row " + (i + 1));
                }

                if (!processedRollNumbers.add(rollNo)) {
                    throw new IllegalArgumentException(
                            "Duplicate enrollment number: " + rollNo);
                }

                BigDecimal marks;

                try {
                    marks = new BigDecimal(marksText);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid marks for " + rollNo
                            + " at Excel row " + (i + 1));
                }

                if (marks.compareTo(BigDecimal.ZERO) < 0
                        || marks.compareTo(BigDecimal.valueOf(maximumMarks)) > 0
                        || marks.stripTrailingZeros().scale() > 2) {

                    throw new IllegalArgumentException(
                            "Invalid marks for " + rollNo
                            + ". Marks must be between 0 and "
                            + maximumMarks + " with at most 2 decimal places.");
                }

                importedMarks.put(student.getStudentId(), marks);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to read the Excel file. Please check the template.");
        }

        if (importedMarks.isEmpty()) {
            throw new IllegalArgumentException("No marks were found in the Excel file.");
        }

        return importedMarks;
    }
}
