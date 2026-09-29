package com.edunest.service;

import com.edunest.dto.mobile.SchoolContactResponse;
import com.edunest.entity.Tenant;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.TenantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MobileSchoolServiceImpl implements MobileSchoolService {

    private final TenantRepository tenantRepository;

    @Override
    public SchoolContactResponse getSchoolContact(Integer tenantId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new CustomException("tenant", "School not found"));

        SchoolContactResponse response = new SchoolContactResponse();
        response.setSchoolName(tenant.getTenantName());
        response.setLogoUrl((tenant.getMobileLogoUrl() != null && !tenant.getMobileLogoUrl().isBlank())
                ? tenant.getMobileLogoUrl()
                : tenant.getLogoUrl());
        response.setContactName(tenant.getContactName());
        response.setContactEmail(tenant.getContactEmail());
        response.setContactPhone(tenant.getContactPhone());
        response.setWebsite(tenant.getDomainName());
        response.setAddress(CommonHelper.fullAddressForTenant(tenant));

        return response;
    }
}
