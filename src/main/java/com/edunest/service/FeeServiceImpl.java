package com.edunest.service;

import com.edunest.configuration.RazorpayConfiguration;
import com.edunest.constant.Constant;
import com.edunest.dto.fee.FeePaymentRequest;
import com.edunest.dto.fee.FeePaymentResponse;
import com.edunest.dto.fee.FeeReceiptDetails;
import com.edunest.dto.fee.FeeStatusResponse;
import com.edunest.dto.fee.StudentFeeDetailResponse;
import com.edunest.dto.mobile.FeeOrderResponse;
import com.edunest.dto.mobile.VerifyPaymentResponse;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FeeServiceImpl implements FeeService {

    @Autowired
    FeePaymentRepository feePaymentRepository;

    @Autowired
    ClassFeeRepository classFeeRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    RazorpayConfiguration razorpayService;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    EmailService emailService;

    @Override
    public List<FeeStatusResponse> getFeeStatus(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        ClassFee classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classId, currentYear.getAcademicYearId(), tenantId);
        BigDecimal annualFee = classFee != null && classFee.getAnnualFee() != null ? classFee.getAnnualFee() : BigDecimal.ZERO;
        BigDecimal hostelFee = classFee != null && classFee.getHostelFee() != null ? classFee.getHostelFee() : BigDecimal.ZERO;

        List<StudentClass> studentClasses = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            studentIds.add(studentClass.getStudentId());
        }

        Map<Integer, BigDecimal> paidByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<FeePayment> payments = feePaymentRepository.findByTenantIdAndAcademicYearIdAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), studentIds);
            for (FeePayment feePayment : payments) {
                BigDecimal existingAmount = paidByStudent.get(feePayment.getStudentId());
                if (existingAmount == null) {
                    existingAmount = BigDecimal.ZERO;
                }
                paidByStudent.put(feePayment.getStudentId(), existingAmount.add(feePayment.getAmount()));
            }
        }

        List<FeeStatusResponse> result = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            Student student = studentRepository.findById(studentClass.getStudentId()).orElse(null);
            boolean isHostel = student != null && Boolean.TRUE.equals(student.getIsHostel());

            // Hostel students owe the class annual fee plus the class hostel fee.
            BigDecimal studentAnnual = isHostel ? annualFee.add(hostelFee) : annualFee;
            BigDecimal paid = paidByStudent.getOrDefault(studentClass.getStudentId(), BigDecimal.ZERO);

            FeeStatusResponse response = new FeeStatusResponse();
            response.setStudentId(studentClass.getStudentId());
            response.setStudentName(CommonHelper.studentNameForStudent(student));
            response.setRollNo(studentClass.getRollNo());
            response.setAnnualFee(studentAnnual);
            response.setPaidAmount(paid);
            response.setDueAmount(studentAnnual.subtract(paid));
            result.add(response);
        }

        result.sort(Comparator.comparing(r -> CommonHelper.rollNo(r.getRollNo())));
        return result;
    }

    @Override
    @Transactional
    public String collectPayment(Integer tenantId, Integer collectedBy, FeePaymentRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(request.getStudentId(), tenantId)
                .orElseThrow(() -> new CustomException("student", "Student is not assigned to a class"));

        String receiptNo = nextReceiptNo(tenantId, currentYear);

        FeePayment payment = new FeePayment();
        payment.setTenantId(tenantId);
        payment.setStudentId(request.getStudentId());
        payment.setClassId(studentClass.getClassId());
        payment.setSectionId(studentClass.getSectionId());
        payment.setAcademicYearId(currentYear.getAcademicYearId());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : Constant.PAYMENT_MODE_CASH);
        payment.setReceiptNo(receiptNo);
        payment.setRemarks(request.getRemarks());
        payment.setCollectedBy(collectedBy);
        feePaymentRepository.save(payment);

        sendFeeReceiptEmail(payment, studentClass);
        return receiptNo;
    }

    @Override
    @Transactional
    public void recordOnlinePayment(Integer tenantId, Integer studentId, BigDecimal amount, String razorpayPaymentId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("student", "Student is not assigned to a class"));

        FeePayment payment = new FeePayment();
        payment.setTenantId(tenantId);
        payment.setStudentId(studentId);
        payment.setClassId(studentClass.getClassId());
        payment.setSectionId(studentClass.getSectionId());
        payment.setAcademicYearId(currentYear.getAcademicYearId());
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMode(Constant.PAYMENT_MODE_ONLINE);
        payment.setReceiptNo(nextReceiptNo(tenantId, currentYear));
        payment.setRemarks("Razorpay payment ID: " + razorpayPaymentId);
        feePaymentRepository.save(payment);

        sendFeeReceiptEmail(payment, studentClass);
    }

    private String nextReceiptNo(Integer tenantId, AcademicYear currentYear) {
        long count = feePaymentRepository.countByTenantIdAndAcademicYearId(tenantId, currentYear.getAcademicYearId());
        return "RC-" + currentYear.getYearName() + "-" + String.format("%05d", count + 1);
    }

    private void sendFeeReceiptEmail(FeePayment payment, StudentClass studentClass) {
        Student student = studentRepository.findById(payment.getStudentId()).orElse(null);
        if (student == null) {
            return;
        }

        String toEmail = (student.getParentEmail() != null && !student.getParentEmail().isBlank())
                ? student.getParentEmail() : student.getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        Tenant tenant = tenantRepository.findById(payment.getTenantId()).orElse(null);
        AcademicYear currentYear = commonHelper.getCurrentYear(payment.getTenantId());

        String collectedByName = "Online Payment";
        if (payment.getCollectedBy() != null) {
            Teacher teacher = teacherRepository.findById(payment.getCollectedBy()).orElse(null);
            collectedByName = teacher != null ? CommonHelper.teacherNameForTeacher(teacher) : "-";
        }

        FeeReceiptDetails details = FeeReceiptDetails.builder()
                .schoolName(tenant != null ? tenant.getTenantName() : "")
                .schoolAddress(tenant != null ? CommonHelper.fullAddressForTenant(tenant) : "")
                .schoolContact(tenant != null ? "Phone: " + tenant.getContactPhone() + " | Email: " + tenant.getContactEmail() : "")
                .receiptNo(payment.getReceiptNo())
                .paymentDateFormatted(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .admissionNo(student.getAdmissionNo())
                .sessionYear(currentYear.getYearName())
                .studentName(CommonHelper.studentNameForStudent(student))
                .displayClass(commonHelper.displayClassForStudentClass(studentClass))
                .paymentMode(payment.getPaymentMode())
                .remarks(payment.getRemarks())
                .collectedBy(collectedByName)
                .amount(payment.getAmount())
                .build();

        String receiptUrl = emailService.sendFeeReceiptEmail(toEmail, details);
        if (receiptUrl != null) {
            payment.setReceiptUrl(receiptUrl);
            feePaymentRepository.save(payment);
        }
    }

    @Override
    public List<FeePaymentResponse> getPaymentHistory(Integer tenantId, Integer studentId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<FeePayment> payments = feePaymentRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdOrderByPaymentDateDescFeePaymentIdDesc(
                        tenantId, studentId, currentYear.getAcademicYearId());

        List<FeePaymentResponse> feePaymentResponses = new ArrayList<>();

        for (FeePayment payment : payments) {
            String collectedByName = null;
            if (payment.getCollectedBy() != null) {
                Teacher teacher = teacherRepository.findById(payment.getCollectedBy()).orElse(null);
                if (teacher != null) {
                    collectedByName = CommonHelper.teacherNameForTeacher(teacher);
                }
            }

            FeePaymentResponse response = new FeePaymentResponse();
            BeanUtils.copyProperties(payment, response);
            response.setCollectedBy(collectedByName);
            feePaymentResponses.add(response);
        }
        return feePaymentResponses;
    }

    @Override
    public StudentFeeDetailResponse getStudentFeeDetail(Integer tenantId, Integer studentId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("studentId", "Student not found"));

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("class", "You are not assigned to a class yet"));

        ClassFee classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(
                studentClass.getClassId(), currentYear.getAcademicYearId(), tenantId);
        BigDecimal annualFee = classFee != null && classFee.getAnnualFee() != null ? classFee.getAnnualFee() : BigDecimal.ZERO;
        BigDecimal hostelFee = classFee != null && classFee.getHostelFee() != null ? classFee.getHostelFee() : BigDecimal.ZERO;
        boolean isHostel = Boolean.TRUE.equals(student.getIsHostel());
        BigDecimal totalFee = isHostel ? annualFee.add(hostelFee) : annualFee;

        List<FeePaymentResponse> payments = getPaymentHistory(tenantId, studentId);
        BigDecimal paidAmount = BigDecimal.ZERO;
        for (FeePaymentResponse payment : payments) {
            paidAmount = paidAmount.add(payment.getAmount());
        }

        StudentFeeDetailResponse response = new StudentFeeDetailResponse();
        response.setStudentId(studentId);
        response.setStudentName(CommonHelper.studentNameForStudent(student));
        response.setDisplayClass(commonHelper.displayClassForStudentClass(studentClass));
        response.setRollNo(studentClass.getRollNo());
        response.setAcademicYearName(currentYear.getYearName());
        response.setTotalFee(totalFee);
        response.setPaidAmount(paidAmount);
        response.setPendingAmount(totalFee.subtract(paidAmount));
        response.setPayments(payments);
        return response;
    }

    @Override
    public FeeOrderResponse createFeeOrder(Integer tenantId, Integer studentId, BigDecimal requestedAmount) {
        BigDecimal pendingAmount = getStudentFeeDetail(tenantId, studentId).getPendingAmount();
        if (pendingAmount == null || pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("amount", "No pending fee to pay");
        }

        BigDecimal amount = requestedAmount != null ? requestedAmount : pendingAmount;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("amount", "Amount must be greater than zero");
        }
        if (amount.compareTo(pendingAmount) > 0) {
            throw new CustomException("amount", "Amount cannot be more than the pending fee");
        }

        String receipt = "FEE-" + studentId + "-" + System.currentTimeMillis();
        RazorpayOrder razorpayOrder = razorpayService.createOrder(tenantId, studentId, amount, "INR", receipt);

        FeeOrderResponse feeOrderResponse = new FeeOrderResponse();
        feeOrderResponse.setRazorpayOrderId(razorpayOrder.getRazorpayOrderId());
        feeOrderResponse.setRazorpayOrderRef(razorpayOrder.getRazorpayOrderRef());
        feeOrderResponse.setAmount(razorpayOrder.getAmount());
        feeOrderResponse.setCurrency(razorpayOrder.getCurrency());
        feeOrderResponse.setKeyId(razorpayService.getKeyId());
        return feeOrderResponse;
    }

    @Override
    @Transactional
    public VerifyPaymentResponse verifyFeePayment(Integer razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        boolean verified = razorpayService.verifyAndRecordPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (verified) {
            RazorpayOrder razorpayOrder = razorpayService.getOrder(razorpayOrderId);
            recordOnlinePayment(razorpayOrder.getTenantId(), razorpayOrder.getStudentId(), razorpayOrder.getAmount(), razorpayPaymentId);
        }

        VerifyPaymentResponse verifyPaymentResponse = new VerifyPaymentResponse();
        verifyPaymentResponse.setVerified(verified);
        verifyPaymentResponse.setStatus(verified ? Constant.PAYMENT_STATUS_PAID : Constant.PAYMENT_STATUS_FAILED);
        return verifyPaymentResponse;
    }

}
