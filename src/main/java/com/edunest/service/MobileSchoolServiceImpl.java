package com.edunest.service;

import com.edunest.dto.mobile.SchoolContactResponse;
import com.edunest.entity.Tenant;
import com.edunest.error.CustomException;
import com.edunest.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MobileSchoolServiceImpl implements MobileSchoolService {

    @Autowired
    TenantRepository tenantRepository;

    @Override
    public SchoolContactResponse getSchoolContact(Integer tenantId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new CustomException("tenant", "School not found"));

        SchoolContactResponse response = new SchoolContactResponse();
        response.setTenantId(tenant.getTenantId());
        response.setSchoolName(tenant.getTenantName());
        response.setLogoUrl(hasText(tenant.getMobileLogoUrl())
                ? tenant.getMobileLogoUrl()
                : tenant.getLogoUrl());
        response.setContactName(tenant.getContactName());
        response.setContactEmail(tenant.getContactEmail());
        response.setContactPhone(tenant.getContactPhone());
        response.setWebsite(tenant.getDomainName());
        response.setAddress(buildFullAddress(tenant));

        return response;
    }

    private String buildFullAddress(Tenant tenant) {
        List<String> parts = new ArrayList<>();

        if (hasText(tenant.getAddressLine1())) parts.add(tenant.getAddressLine1().trim());
        if (hasText(tenant.getAddressLine2())) parts.add(tenant.getAddressLine2().trim());
        if (hasText(tenant.getCity())) parts.add(tenant.getCity().trim());
        if (hasText(tenant.getState())) parts.add(tenant.getState().trim());

        String address = String.join(", ", parts);

        if (hasText(tenant.getPostalCode())) {
            address = address.isEmpty()
                    ? tenant.getPostalCode().trim()
                    : address + " - " + tenant.getPostalCode().trim();
        }

        return address.isEmpty() ? null : address;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
