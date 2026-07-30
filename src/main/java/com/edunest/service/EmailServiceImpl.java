package com.edunest.service;

import com.edunest.dto.mobile.StudentResetCredential;
import com.edunest.error.CustomException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private String loadTemplate(String templateName) throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void sendResetEmail(String toEmail, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String teacherName, String newPassword) {
        try {
            String displayName = (teacherName != null && !teacherName.isBlank()) ? teacherName : "Teacher";
            String html = loadTemplate("sendPasswordResetEmail.html")
                    .replace("{{displayName}}", displayName)
                    .replace("{{newPassword}}", newPassword);

            sendResetEmail(toEmail, "EduNest - Your Password Has Been Reset", html);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
            throw new CustomException("Email", "Failed to send password reset email. Please try again later.");
        }
    }

    @Override
    public void sendStudentPasswordResetEmail(String toEmail, List<StudentResetCredential> accounts) {
        try {
            String multiAccountNotice = accounts.size() > 1
                    ? "<p>This email is linked to <b>" + accounts.size()
                            + "</b> student accounts. New credentials for each are listed below.</p>"
                    : "";

            StringBuilder accountRows = new StringBuilder();
            for (StudentResetCredential account : accounts) {
                accountRows.append("<tr>")
                        .append("<td>").append(account.getStudentName()).append("</td>")
                        .append("<td><b>").append(account.getUsername()).append("</b></td>")
                        .append("<td><b>").append(account.getNewPassword()).append("</b></td>")
                        .append("</tr>");
            }

            String html = loadTemplate("sendStudentPasswordResetEmail.html")
                    .replace("{{multiAccountNotice}}", multiAccountNotice)
                    .replace("{{accountRows}}", accountRows.toString());

            sendResetEmail(toEmail, "EduNest - Your Password Has Been Reset", html);
            log.info("Student password reset email sent to {} for {} account(s)", toEmail, accounts.size());
        } catch (Exception e) {
            log.error("Failed to send student password reset email to {}", toEmail, e);
            throw new CustomException("Email", "Failed to send password reset email. Please try again later.");
        }
    }
}
