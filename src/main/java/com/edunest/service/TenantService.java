package com.edunest.service;

import com.edunest.entity.TenantFeeSetting;

public interface TenantService {

    TenantFeeSetting getFeeSetting(Integer tenantId);

    boolean saveFeeSetting(Integer tenantId, TenantFeeSetting request);
}
