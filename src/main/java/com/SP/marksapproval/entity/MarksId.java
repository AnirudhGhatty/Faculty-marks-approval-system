package com.SP.marksapproval.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MarksId implements Serializable {

    private Integer studentId;
    private Integer subjectId;

    public MarksId() {
    }

    public MarksId(Integer studentId, Integer subjectId) {
        this.studentId = studentId;
        this.subjectId = subjectId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof MarksId)) return false;

        MarksId marksId = (MarksId) o;

        return Objects.equals(studentId, marksId.studentId)
                && Objects.equals(subjectId, marksId.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, subjectId);
    }
}