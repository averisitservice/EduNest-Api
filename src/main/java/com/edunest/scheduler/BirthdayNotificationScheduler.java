package com.edunest.scheduler;

import com.edunest.constant.Constant;
import com.edunest.entity.Student;
import com.edunest.repository.StudentRepository;
import com.edunest.service.StudentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
public class BirthdayNotificationScheduler {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentNotificationService studentNotificationService;

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void sendBirthdayNotifications() {
        List<Student> birthdayStudents = studentRepository.findTodaysBirthdays();

        for (Student student : birthdayStudents) {
            int age = Period.between(student.getDateOfBirth(), LocalDate.now()).getYears();

            String title = "Happy Birthday " + student.getFirstName() + "! 🎉";
            String body = "Turning " + age + " today! Wishing you a wonderful birthday filled with happiness and success!";

            studentNotificationService.notify(student.getTenantId(), List.of(student.getStudentId()), Constant.NOTIFICATION_TYPE_BIRTHDAY, student.getStudentId(), title, body);
        }
    }
}
