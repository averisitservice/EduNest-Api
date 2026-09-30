package com.edunest.dto;

import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;

public interface AttendanceRosterProjection {
    Student getStudent();

    StudentClass getStudentClass();
}
