package com.edunest.service;

import com.edunest.entity.TenantFeeSetting;

public interface TenantService {

    TenantFeeSetting getTenantFeeSetting (Integer tenantId);

    boolean saveTenantFeeSetting(Integer tenantId, TenantFeeSetting request);
}
