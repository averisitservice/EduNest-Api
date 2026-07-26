package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolContactResponse {
    private Integer tenantId;
    private String schoolName;
    private String logoUrl;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String address;
}
