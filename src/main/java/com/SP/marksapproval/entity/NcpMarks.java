
package com.SP.marksapproval.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "ncp_marks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ncp_marks_student_subject",
            columnNames = {"student_id", "subject_id"}
        )
    }
)
public class NcpMarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ncp_mark_id")
    private Integer ncpMarkId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "ncp1")
    private Integer ncp1;

    @Column(name = "ncp2")
    private Integer ncp2;

    @Column(name = "ncp3")
    private Integer ncp3;

    @Column(name = "ncp1_locked", nullable = false)
    private Boolean ncp1Locked = false;

    @Column(name = "ncp2_locked", nullable = false)
    private Boolean ncp2Locked = false;

    @Column(name = "ncp3_locked", nullable = false)
    private Boolean ncp3Locked = false;

    public NcpMarks() {
    }

    public Integer getNcpMarkId() {
        return ncpMarkId;
    }

    public void setNcpMarkId(Integer ncpMarkId) {
        this.ncpMarkId = ncpMarkId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Integer getNcp1() {
        return ncp1;
    }

    public void setNcp1(Integer ncp1) {
        this.ncp1 = ncp1;
    }

    public Integer getNcp2() {
        return ncp2;
    }

    public void setNcp2(Integer ncp2) {
        this.ncp2 = ncp2;
    }

    public Integer getNcp3() {
        return ncp3;
    }

    public void setNcp3(Integer ncp3) {
        this.ncp3 = ncp3;
    }

    public Boolean getNcp1Locked() {
        return ncp1Locked;
    }

    public void setNcp1Locked(Boolean ncp1Locked) {
        this.ncp1Locked = ncp1Locked;
    }

    public Boolean getNcp2Locked() {
        return ncp2Locked;
    }

    public void setNcp2Locked(Boolean ncp2Locked) {
        this.ncp2Locked = ncp2Locked;
    }

    public Boolean getNcp3Locked() {
        return ncp3Locked;
    }

    public void setNcp3Locked(Boolean ncp3Locked) {
        this.ncp3Locked = ncp3Locked;
    }
}
