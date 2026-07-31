package com.edunest.service;

import com.edunest.dto.fee.FeePaymentRequest;
import com.edunest.dto.fee.FeePaymentResponse;
import com.edunest.dto.fee.FeeStatusResponse;
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
                paidByStudent.merge(feePayment.getStudentId(), feePayment.getAmount(), BigDecimal::add);
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
            response.setStudentName(student != null ? student.getFirstName() + " " + student.getLastName() : null);
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
                    collectedByName = teacher.getFirstName() + " " + teacher.getLastName();
                }
            }

            FeePaymentResponse response = new FeePaymentResponse();
            BeanUtils.copyProperties(payment, response);
            response.setCollectedBy(collectedByName);
            feePaymentResponses.add(response);
        }
        return feePaymentResponses;
    }
}
