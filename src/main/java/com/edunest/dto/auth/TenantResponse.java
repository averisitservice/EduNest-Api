package com.edunest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private Integer tenantId;
    private String schoolCode;
    private String tenantName;
    private String schoolBannerUrl;
    private String mobileLogoUrl;
    private String logoUrl;
    private String singleLogoUrl;
    private String primaryColor;
    private String faviconUrl;
    private String principalSignUrl;
    private Boolean isHostel;
}
