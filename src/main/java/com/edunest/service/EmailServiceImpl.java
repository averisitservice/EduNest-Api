package com.edunest.service;

import com.edunest.dto.fee.FeeReceiptDetails;
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

import com.edunest.dto.fee.ByteArrayMultipartFile;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.apache.commons.text.WordUtils;
import java.util.Locale;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FileStorageService fileStorageService;

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
            String html = loadTemplate("sendPassword.html")
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

            String html = loadTemplate("sendStudentPassword.html")
                    .replace("{{multiAccountNotice}}", multiAccountNotice)
                    .replace("{{accountRows}}", accountRows.toString());

            sendResetEmail(toEmail, "EduNest - Your Password Has Been Reset", html);
            log.info("Student password reset email sent to {} for {} account(s)", toEmail, accounts.size());
        } catch (Exception e) {
            log.error("Failed to send student password reset email to {}", toEmail, e);
            throw new CustomException("Email", "Failed to send password reset email. Please try again later.");
        }
    }

    @Override
    public void sendStudentWelcomeEmail(String toEmail, String studentName, String username, String password) {
        try {
            String html = loadTemplate("sendStudentWelcome.html")
                    .replace("{{studentName}}", studentName != null ? studentName : "Student")
                    .replace("{{username}}", username != null ? username : "")
                    .replace("{{password}}", password != null ? password : "");

            sendResetEmail(toEmail, "EduNest - Mobile App Login Credentials", html);
            log.info("Student login credentials email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send student login credentials email to {}", toEmail, e);
        }
    }

    @Override
    public String sendFeeReceiptEmail(String toEmail, FeeReceiptDetails details) {
        try {
            String amountFormatted = formatIndianCurrency(details.getAmount());

            // 1. Generate normal email HTML body from separate template
            String emailHtml = loadTemplate("feeReceiptEmail.html")
                    .replace("{{schoolName}}", nullToDash(details.getSchoolName()))
                    .replace("{{receiptNo}}", nullToDash(details.getReceiptNo()))
                    .replace("{{paymentDateFormatted}}", nullToDash(details.getPaymentDateFormatted()))
                    .replace("{{studentName}}", nullToDash(details.getStudentName()))
                    .replace("{{displayClass}}", nullToDash(details.getDisplayClass()))
                    .replace("{{paymentMode}}", nullToDash(details.getPaymentMode()))
                    .replace("{{amount}}", amountFormatted);

            // 2. Generate PDF HTML from feeReceipt.html template
            String pdfTemplateHtml = loadTemplate("feeReceipt.html")
                    .replace("{{schoolName}}", nullToDash(details.getSchoolName()))
                    .replace("{{schoolAddress}}", nullToDash(details.getSchoolAddress()))
                    .replace("{{receiptNo}}", nullToDash(details.getReceiptNo()))
                    .replace("{{paymentDateFormatted}}", nullToDash(details.getPaymentDateFormatted()))
                    .replace("{{admissionNo}}", nullToDash(details.getAdmissionNo()))
                    .replace("{{sessionYear}}", nullToDash(details.getSessionYear()))
                    .replace("{{studentName}}", nullToDash(details.getStudentName()))
                    .replace("{{displayClass}}", nullToDash(details.getDisplayClass()))
                    .replace("{{remarks}}", nullToDash(details.getRemarks()))
                    .replace("{{collectedBy}}", nullToDash(details.getCollectedBy()))
                    .replace("{{amountWords}}", numberToWords(details.getAmount()))
                    .replace("{{amount}}", amountFormatted);

            byte[] pdfBytes;
            try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(pdfTemplateHtml, null);
                builder.toStream(pdfOutputStream);
                builder.run();
                pdfBytes = pdfOutputStream.toByteArray();
            }

            String originalFileName = "Receipt_" + details.getReceiptNo() + ".pdf";
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(pdfBytes, originalFileName,
                    "application/pdf");
            Map<String, Object> uploadResult = fileStorageService.uploadFile(multipartFile, "edunest/receipt");
            String receiptUrl = String.valueOf(uploadResult.get("secure_url"));

            String attachmentName = "FeeReceipt_" + details.getReceiptNo() + ".pdf";
            String studentName = details.getStudentName() != null ? details.getStudentName() : "";
            String subject = "Fee Payment Receipt – " + studentName;
            sendEmailWithAttachment(toEmail, subject, emailHtml, attachmentName, pdfBytes);
            log.info("Fee receipt email sent to {} for receipt {} with PDF attachment. URL: {}", toEmail,
                    details.getReceiptNo(), receiptUrl);

            return receiptUrl;
        } catch (Exception e) {
            log.error("Failed to process fee receipt PDF or send email to {}", toEmail, e);
            return null;
        }
    }

    private void sendEmailWithAttachment(String toEmail, String subject, String html, String attachmentName,
            byte[] attachmentBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes), "application/pdf");

        mailSender.send(message);
    }

    private String nullToDash(String value) {
        return (value != null && !value.isBlank()) ? value : "-";
    }

    private String formatIndianCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        DecimalFormat format = new DecimalFormat("##,##,###");
        return format.format(amount.longValue());
    }

    private String numberToWords(BigDecimal amount) {
        long value = amount != null ? amount.longValue() : 0;
        if (value == 0) {
            return "Zero Rupees Only";
        }
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.ENGLISH, RuleBasedNumberFormat.SPELLOUT);
        String words = formatter.format(value);
        return WordUtils.capitalizeFully(words, ' ', '-') + " Rupees Only";
    }
}
