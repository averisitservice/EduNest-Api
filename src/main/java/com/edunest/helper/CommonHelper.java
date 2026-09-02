package com.edunest.helper;

import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSectionRepository;
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

    @Autowired
    private ClassMasterRepository classMasterRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    public AcademicYear getCurrentYear(Integer tenantId) {
        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        if (currentYear == null) {
            throw new CustomException("academicYear", "No active academic year found");
        }
        return currentYear;
    }

    public String teacherNameForId(Integer teacherId) {
        if (teacherId == null) {
            return null;
        }
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        return teacher != null ? teacher.getTeacherName() : null;
    }

    public static String teacherNameForTeacher(Teacher teacher) {
        return teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : null;
    }

    public String studentNameForId(Integer studentId) {
        if (studentId == null) {
            return null;
        }
        Student student = studentRepository.findById(studentId).orElse(null);
        return studentNameForStudent(student);
    }

    public static String studentNameForStudent(Student student) {
        return student != null ? student.getFirstName() + " " + student.getLastName() : null;
    }

    public static String rollNo(String rollNo) {
        if (rollNo.trim().matches("\\d+")) {
            return String.format("%010d", Long.parseLong(rollNo.trim()));
        }
        return rollNo.trim();
    }

    public static String fullAddressForTenant(Tenant tenant) {
        return fullAddressForCommon(tenant.getAddressLine1(), tenant.getAddressLine2(), tenant.getCity(),
                tenant.getState(), tenant.getPostalCode());
    }

    public static String fullAddressForStudent(Student student) {
        return fullAddressForCommon(student.getAddressLine1(), null, student.getCity(),
                student.getState(), student.getPostalCode());
    }

    private static String fullAddressForCommon(String addressLine1, String addressLine2, String city, String state,
            String postalCode) {
        List<String> parts = new ArrayList<>();

        if (hasText(addressLine1))
            parts.add(addressLine1.trim());
        if (hasText(addressLine2))
            parts.add(addressLine2.trim());
        if (hasText(city))
            parts.add(city.trim());
        if (hasText(state))
            parts.add(state.trim());

        String address = String.join(", ", parts);

        if (hasText(postalCode)) {
            address = address.isEmpty()
                    ? postalCode.trim()
                    : address + " - " + postalCode.trim();
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

    public String displayClassForIds(Integer classId, Integer sectionId) {
        if (classId == null) {
            return null;
        }
        ClassMaster classMaster = classMasterRepository.findById(classId).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;
        String sectionName = null;
        if (sectionId != null) {
            ClassSection classSection = classSectionRepository.findById(sectionId).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }
        return (className != null && sectionName != null) ? className + " - " + sectionName : className;
    }

    public String displayClassForStudentClass(StudentClass studentClass) {
        return studentClass != null
                ? displayClassForIds(studentClass.getClassId(), studentClass.getSectionId())
                : null;
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

    public List<Integer> convertClassIdsStringToList(String classIds) {
        List<Integer> result = new ArrayList<>();
        if (classIds == null || classIds.isBlank()) {
            return result;
        }
        for (String id : classIds.split(",")) {
            String trimmedId = id.trim();
            if (!trimmedId.isEmpty()) {
                result.add(Integer.valueOf(trimmedId));
            }
        }
        return result;
    }
}
