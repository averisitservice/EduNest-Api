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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
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
    public void sendFeeReceiptEmail(String toEmail, FeeReceiptDetails details) {
        try {
            String amountFormatted = formatIndianCurrency(details.getAmount());

            String html = loadTemplate("feeReceipt.html")
                    .replace("{{schoolName}}", nullToDash(details.getSchoolName()))
                    .replace("{{schoolAddress}}", nullToDash(details.getSchoolAddress()))
                    .replace("{{schoolContact}}", nullToDash(details.getSchoolContact()))
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

            sendResetEmail(toEmail, "EduNest - Fee Payment Receipt (" + details.getReceiptNo() + ")", html);
            log.info("Fee receipt email sent to {} for receipt {}", toEmail, details.getReceiptNo());
        } catch (Exception e) {
            log.error("Failed to send fee receipt email to {}", toEmail, e);
        }
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

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private String numberToWords(BigDecimal amount) {
        long value = amount != null ? amount.longValue() : 0;
        if (value == 0) {
            return "Zero Rupees Only";
        }

        StringBuilder words = new StringBuilder();
        long crore = value / 10000000;
        value %= 10000000;
        long lakh = value / 100000;
        value %= 100000;
        long thousand = value / 1000;
        value %= 1000;
        long hundred = value / 100;
        long remainder = value % 100;

        if (crore > 0) {
            words.append(twoDigitWords((int) crore)).append(" Crore ");
        }
        if (lakh > 0) {
            words.append(twoDigitWords((int) lakh)).append(" Lakh ");
        }
        if (thousand > 0) {
            words.append(twoDigitWords((int) thousand)).append(" Thousand ");
        }
        if (hundred > 0) {
            words.append(ONES[(int) hundred]).append(" Hundred ");
        }
        if (remainder > 0) {
            words.append(twoDigitWords((int) remainder)).append(" ");
        }

        return words.toString().trim() + " Rupees Only";
    }

    private String twoDigitWords(int number) {
        if (number < 20) {
            return ONES[number];
        }
        int tens = number / 10;
        int ones = number % 10;
        return ones > 0 ? TENS[tens] + " " + ONES[ones] : TENS[tens];
    }
}
