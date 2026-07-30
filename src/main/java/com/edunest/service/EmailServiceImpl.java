package com.edunest.service;

import com.edunest.dto.mobile.StudentResetCredential;
import com.edunest.error.CustomException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private void sendResetEmail(String toEmail, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("EduNest - Your Password Has Been Reset");
        helper.setText(html, true);

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String teacherName, String newPassword) {
        try {
            String displayName = (teacherName != null && !teacherName.isBlank()) ? teacherName : "Teacher";
            String html =
                    "<p>Dear <b>" + displayName + "</b>,</p>" +
                    "<p>Your password has been reset as requested.</p>" +
                    "<p>Your new temporary password is: <b>" + newPassword + "</b></p>" +
                    "<p>Please log in using this password and change it as soon as possible for your security.</p>" +
                    "<p>If you did not request this change, please contact your administrator immediately.</p>" +
                    "<p>Regards,<br>EduNest Team</p>";

            sendResetEmail(toEmail, html);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
            throw new CustomException("Email", "Failed to send password reset email. Please try again later.");
        }
    }

    @Override
    public void sendStudentPasswordResetEmail(String toEmail, List<StudentResetCredential> accounts) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<p>Dear Parent/Student,</p>");
            html.append("<p>The password for the student account(s) registered with this email address ")
                    .append("has been reset as requested.</p>");

            if (accounts.size() > 1) {
                html.append("<p>This email is linked to <b>").append(accounts.size())
                        .append("</b> student accounts. New credentials for each are listed below.</p>");
            }

            html.append("<table cellpadding='8' cellspacing='0' border='1' ")
                    .append("style='border-collapse:collapse;font-family:Arial,sans-serif;font-size:14px;'>")
                    .append("<tr style='background-color:#f0f4f8;'>")
                    .append("<th align='left'>Student</th>")
                    .append("<th align='left'>Username</th>")
                    .append("<th align='left'>New Password</th>")
                    .append("</tr>");

            for (StudentResetCredential account : accounts) {
                html.append("<tr>")
                        .append("<td>").append(account.getStudentName()).append("</td>")
                        .append("<td><b>").append(account.getUsername()).append("</b></td>")
                        .append("<td><b>").append(account.getNewPassword()).append("</b></td>")
                        .append("</tr>");
            }

            html.append("</table>");
            html.append("<p>Please log in to the EduNest app using the username and temporary password above, ")
                    .append("and change your password as soon as possible for your security.</p>");
            html.append("<p>If you did not request this change, please contact your school immediately.</p>");
            html.append("<p>Regards,<br>EduNest Team</p>");

            sendResetEmail(toEmail, html.toString());
            log.info("Student password reset email sent to {} for {} account(s)", toEmail, accounts.size());
        } catch (Exception e) {
            log.error("Failed to send student password reset email to {}", toEmail, e);
            throw new CustomException("Email", "Failed to send password reset email. Please try again later.");
        }
    }
}
