package com.edunest.service;

import com.edunest.dto.fee.FeePaymentRequest;
import com.edunest.dto.fee.FeePaymentResponse;
import com.edunest.dto.fee.FeeStatusResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.ClassFee;
import com.edunest.entity.FeePayment;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.entity.Teacher;
import com.edunest.error.CustomException;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.ClassFeeRepository;
import com.edunest.repository.FeePaymentRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    AcademicYearRepository academicYearRepository;

    private AcademicYear getCurrentYear(Integer tenantId) {
        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        if (currentYear == null) {
            throw new CustomException("academicYear", "No active academic year found");
        }
        return currentYear;
    }

    @Override
    public List<FeeStatusResponse> getFeeStatus(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = getCurrentYear(tenantId);

        ClassFee classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classId, currentYear.getAcademicYearId(), tenantId);
        BigDecimal annualFee = classFee != null && classFee.getAnnualFee() != null ? classFee.getAnnualFee() : BigDecimal.ZERO;
        BigDecimal hostelFee = classFee != null && classFee.getHostelFee() != null ? classFee.getHostelFee() : BigDecimal.ZERO;

        List<StudentClass> roster = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass sc : roster) {
            studentIds.add(sc.getStudentId());
        }

        Map<Integer, BigDecimal> paidByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<FeePayment> payments = feePaymentRepository.findByTenantIdAndAcademicYearIdAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), studentIds);
            for (FeePayment p : payments) {
                paidByStudent.merge(p.getStudentId(), p.getAmount(), BigDecimal::add);
            }
        }

        List<FeeStatusResponse> result = new ArrayList<>();
        for (StudentClass sc : roster) {
            Student student = studentRepository.findById(sc.getStudentId()).orElse(null);
            boolean isHostel = student != null && Boolean.TRUE.equals(student.getIsHostel());

            // Hostel students owe the class annual fee plus the class hostel fee.
            BigDecimal studentAnnual = isHostel ? annualFee.add(hostelFee) : annualFee;
            BigDecimal paid = paidByStudent.getOrDefault(sc.getStudentId(), BigDecimal.ZERO);

            FeeStatusResponse response = new FeeStatusResponse();
            response.setStudentId(sc.getStudentId());
            response.setStudentName(student != null ? student.getFirstName() + " " + student.getLastName() : null);
            response.setRollNo(sc.getRollNo());
            response.setAnnualFee(studentAnnual);
            response.setPaidAmount(paid);
            response.setDueAmount(studentAnnual.subtract(paid));
            result.add(response);
        }

        result.sort(Comparator.comparing(r -> rollNoKey(r.getRollNo())));
        return result;
    }

    @Override
    @Transactional
    public String collectPayment(Integer tenantId, Integer collectedBy, FeePaymentRequest request) {
        AcademicYear currentYear = getCurrentYear(tenantId);

        if (request.getStudentId() == null) {
            throw new CustomException("studentId", "Student is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("amount", "Amount must be greater than zero");
        }

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(request.getStudentId(), tenantId)
                .orElseThrow(() -> new CustomException("student", "Student is not assigned to a class"));

        long count = feePaymentRepository.countByTenantIdAndAcademicYearId(tenantId, currentYear.getAcademicYearId());
        String receiptNo = "RC-" + currentYear.getYearName() + "-" + String.format("%05d", count + 1);

        FeePayment payment = new FeePayment();
        payment.setTenantId(tenantId);
        payment.setStudentId(request.getStudentId());
        payment.setClassId(studentClass.getClassId());
        payment.setSectionId(studentClass.getSectionId());
        payment.setAcademicYearId(currentYear.getAcademicYearId());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : "CASH");
        payment.setReceiptNo(receiptNo);
        payment.setRemarks(request.getRemarks());
        payment.setCollectedBy(collectedBy);
        feePaymentRepository.save(payment);

        return receiptNo;
    }

    @Override
    public List<FeePaymentResponse> getPaymentHistory(Integer tenantId, Integer studentId) {
        AcademicYear currentYear = getCurrentYear(tenantId);

        List<FeePayment> payments = feePaymentRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdOrderByPaymentDateDescFeePaymentIdDesc(
                        tenantId, studentId, currentYear.getAcademicYearId());

        List<FeePaymentResponse> result = new ArrayList<>();
        for (FeePayment p : payments) {
            String collectedByName = null;
            if (p.getCollectedBy() != null) {
                Teacher teacher = teacherRepository.findById(p.getCollectedBy()).orElse(null);
                if (teacher != null) {
                    collectedByName = teacher.getFirstName() + " " + teacher.getLastName();
                }
            }

            FeePaymentResponse response = new FeePaymentResponse();
            response.setFeePaymentId(p.getFeePaymentId());
            response.setAmount(p.getAmount());
            response.setPaymentDate(p.getPaymentDate());
            response.setPaymentMode(p.getPaymentMode());
            response.setReceiptNo(p.getReceiptNo());
            response.setRemarks(p.getRemarks());
            response.setCollectedBy(collectedByName);
            result.add(response);
        }
        return result;
    }

    private String rollNoKey(String rollNo) {
        if (rollNo == null || rollNo.isBlank()) {
            return "zzzzzzzzzz";
        }
        String trimmed = rollNo.trim();
        if (trimmed.matches("\\d+")) {
            return String.format("%010d", Long.parseLong(trimmed));
        }
        return trimmed;
    }
}
