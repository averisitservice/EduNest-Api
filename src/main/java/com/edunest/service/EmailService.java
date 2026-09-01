package com.edunest.service;

import com.edunest.dto.fee.FeeReceiptDetails;
import com.edunest.dto.mobile.StudentResetCredential;

import java.util.List;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String teacherName, String newPassword);

    void sendStudentPasswordResetEmail(String toEmail, List<StudentResetCredential> accounts);

    void sendStudentWelcomeEmail(String toEmail, String studentName, String username, String password);

    String sendFeeReceiptEmail(String toEmail, FeeReceiptDetails details);
}
