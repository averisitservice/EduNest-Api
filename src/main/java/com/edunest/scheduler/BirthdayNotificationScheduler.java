package com.edunest.scheduler;

import com.edunest.entity.ClassMaster;
import com.edunest.entity.ClassSection;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.entity.Tenant;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSectionRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.TenantRepository;
import com.edunest.service.FcmPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BirthdayNotificationScheduler {

    private static final DateTimeFormatter BIRTHDAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM");

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    FcmPushService fcmPushService;

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void sendBirthdayNotifications() {
        List<Student> birthdayStudents = studentRepository.findTodaysBirthdays();

        Map<Integer, List<Student>> studentsByTenant = new HashMap<>();
        for (Student student : birthdayStudents) {
            List<Student> tenantStudents = studentsByTenant.get(student.getTenantId());
            if (tenantStudents == null) {
                tenantStudents = new ArrayList<>();
                studentsByTenant.put(student.getTenantId(), tenantStudents);
            }
            tenantStudents.add(student);
        }

        for (Map.Entry<Integer, List<Student>> entry : studentsByTenant.entrySet()) {
            Integer tenantId = entry.getKey();
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            String schoolName = tenant != null ? tenant.getTenantName() : "";

            for (Student student : entry.getValue()) {
                sendBirthdayCard(tenantId, student, schoolName);
            }
        }
    }

    private void sendBirthdayCard(Integer tenantId, Student student, String schoolName) {
        int age = Period.between(student.getDateOfBirth(), LocalDate.now()).getYears();
        String studentName = student.getFirstName() + " " + student.getLastName();
        String displayClass = buildDisplayClass(student.getStudentId(), tenantId);
        String birthdayDate = student.getDateOfBirth().format(BIRTHDAY_DATE_FORMAT);

        Map<String, String> data = new HashMap<>();
        data.put("type", "BIRTHDAY_CARD");
        data.put("studentId", String.valueOf(student.getStudentId()));
        data.put("studentName", studentName);
        data.put("displayClass", displayClass != null ? displayClass : "");
        data.put("age", String.valueOf(age));
        data.put("birthdayDate", birthdayDate);
        data.put("schoolName", schoolName != null ? schoolName : "");
        data.put("photoUrl", student.getPhotoUrl() != null ? student.getPhotoUrl() : "");

        String title = "Happy Birthday " + student.getFirstName() + "! 🎉";
        String body = "Wishing you a wonderful birthday filled with happiness and success!";

        fcmPushService.sendToStudents(tenantId, List.of(student.getStudentId()), title, body, data);
    }

    private String buildDisplayClass(Integer studentId, Integer tenantId) {
        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId).orElse(null);
        if (studentClass == null) {
            return null;
        }

        ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;

        String sectionName = null;
        if (studentClass.getSectionId() != null) {
            ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }

        return (className != null && sectionName != null) ? className + " - " + sectionName : className;
    }
}
