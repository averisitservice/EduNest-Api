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
    public TenantFeeSetting getTenantFeeSetting (Integer tenantId) {

        AcademicYear year = commonHelper.getCurrentYear(tenantId);

        TenantFeeSetting tenantFeeSetting = tenantFeeSettingRepository.findByTenantIdAndAcademicYearId(tenantId, year.getAcademicYearId()).orElse(null);

        if (tenantFeeSetting == null) {
            tenantFeeSetting = new TenantFeeSetting();
            tenantFeeSetting.setTenantId(tenantId);
            tenantFeeSetting.setAcademicYearId(year.getAcademicYearId());
            tenantFeeSetting.setPaymentFrequency(Constant.PAYMENT_FREQUENCY_ANNUAL);
            tenantFeeSetting.setDueDayOfMonth(10);
            tenantFeeSetting.setGracePeriodDays(10);
            tenantFeeSetting.setOverdueChargeAmount(BigDecimal.ZERO);
        }

        return tenantFeeSetting;
    }

    @Override
    @Transactional
    public boolean saveTenantFeeSetting(Integer tenantId, TenantFeeSetting request) {
        AcademicYear academicYear = commonHelper.getCurrentYear(tenantId);
        TenantFeeSetting tenantFeeSetting = tenantFeeSettingRepository.findByTenantIdAndAcademicYearId(tenantId, academicYear.getAcademicYearId()).orElse(new TenantFeeSetting());

        tenantFeeSetting.setTenantId(tenantId);
        tenantFeeSetting.setAcademicYearId(academicYear.getAcademicYearId());
        tenantFeeSetting.setPaymentFrequency(request.getPaymentFrequency() != null ? request.getPaymentFrequency() : Constant.PAYMENT_FREQUENCY_ANNUAL);
        tenantFeeSetting.setDueDayOfMonth(request.getDueDayOfMonth() != null ? request.getDueDayOfMonth() : 10);
        tenantFeeSetting.setGracePeriodDays(request.getGracePeriodDays() != null ? request.getGracePeriodDays() : 10);
        tenantFeeSetting.setOverdueChargeAmount(request.getOverdueChargeAmount() != null ? request.getOverdueChargeAmount() : BigDecimal.ZERO);

        tenantFeeSettingRepository.save(tenantFeeSetting);

        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant != null) {
            tenant.setPaymentFrequency(tenantFeeSetting.getPaymentFrequency());
            tenantRepository.save(tenant);
        }
        return true;
    }
}
