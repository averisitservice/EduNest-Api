package com.edunest.helper;

import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.SubjectRepository;
import com.edunest.repository.TeacherRepository;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommonHelper {

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public AcademicYear getCurrentYear(Integer tenantId) {
        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        if (currentYear == null) {
            throw new CustomException("academicYear", "No active academic year found");
        }
        return currentYear;
    }

    public String teacherName(Integer teacherId) {
        if (teacherId == null) {
            return null;
        }
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        return teacher != null ? teacher.getTeacherName() : null;
    }

    public String studentName(Integer studentId) {
        if (studentId == null) {
            return null;
        }
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null ? student.getFirstName() + " " + student.getLastName() : null;
    }

    public static String rollNo(String rollNo) {
        if (rollNo.trim().matches("\\d+")) {
            return String.format("%010d", Long.parseLong(rollNo.trim()));
        }
        return rollNo.trim();
    }

    public static String fullAddress(Tenant tenant) {
        List<String> parts = new ArrayList<>();

        if (hasText(tenant.getAddressLine1())) parts.add(tenant.getAddressLine1().trim());
        if (hasText(tenant.getAddressLine2())) parts.add(tenant.getAddressLine2().trim());
        if (hasText(tenant.getCity())) parts.add(tenant.getCity().trim());
        if (hasText(tenant.getState())) parts.add(tenant.getState().trim());

        String address = String.join(", ", parts);

        if (hasText(tenant.getPostalCode())) {
            address = address.isEmpty()
                    ? tenant.getPostalCode().trim()
                    : address + " - " + tenant.getPostalCode().trim();
        }

        return address.isEmpty() ? null : address;
    }

    public static String generateRandomPassword() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();
        return generator.generate(8);
    }

    public String subjectName(Integer subjectId) {
        if (subjectId == null) {
            return null;
        }
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        return subject != null ? subject.getSubjectName() : null;
    }

    public String generateAdmissionNo(Integer tenantId) {
        String year = String.valueOf(LocalDate.now().getYear());
        String lastNo = studentRepository.findLastAdmissionNo(tenantId, year);
        int sequence = 1;
        if (lastNo != null) {
            sequence = Integer.parseInt(lastNo.split("-")[1]) + 1;
        }
        return year + "-" + String.format("%03d", sequence);
    }

    public static String generateUsername(String firstName, LocalDate dob) {
        String day = String.format("%02d", dob.getDayOfMonth());
        String month = String.format("%02d", dob.getMonthValue());
        return firstName + day + month;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
