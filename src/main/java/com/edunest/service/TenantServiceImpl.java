package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Tenant;
import com.edunest.entity.TenantFeeSetting;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.TenantFeeSettingRepository;
import com.edunest.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantFeeSettingRepository tenantFeeSettingRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CommonHelper commonHelper;

    @Override
    public TenantFeeSetting getFeeSetting(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        return tenantFeeSettingRepository
                .findByTenantIdAndAcademicYearId(tenantId, currentYear.getAcademicYearId())
                .orElseGet(() -> TenantFeeSetting.builder()
                        .tenantId(tenantId)
                        .academicYearId(currentYear.getAcademicYearId())
                        .paymentFrequency(Constant.PAYMENT_FREQUENCY_ANNUAL)
                        .dueDayOfMonth(10)
                        .gracePeriodDays(10)
                        .overdueChargeAmount(BigDecimal.ZERO)
                        .build());
    }

    @Override
    @Transactional
    public boolean saveFeeSetting(Integer tenantId, TenantFeeSetting request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        TenantFeeSetting setting = tenantFeeSettingRepository
                .findByTenantIdAndAcademicYearId(tenantId, currentYear.getAcademicYearId())
                .orElseGet(() -> TenantFeeSetting.builder()
                        .tenantId(tenantId)
                        .academicYearId(currentYear.getAcademicYearId())
                        .build());

        setting.setPaymentFrequency(request.getPaymentFrequency() != null ? request.getPaymentFrequency() : Constant.PAYMENT_FREQUENCY_ANNUAL);
        setting.setDueDayOfMonth(request.getDueDayOfMonth() != null ? request.getDueDayOfMonth() : 10);
        setting.setGracePeriodDays(request.getGracePeriodDays() != null ? request.getGracePeriodDays() : 10);
        setting.setOverdueChargeAmount(request.getOverdueChargeAmount() != null ? request.getOverdueChargeAmount() : BigDecimal.ZERO);

        tenantFeeSettingRepository.save(setting);

        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant != null) {
            tenant.setPaymentFrequency(setting.getPaymentFrequency());
            tenantRepository.save(tenant);
        }

        return true;
    }
}
