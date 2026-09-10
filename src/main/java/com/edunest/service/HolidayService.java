package com.edunest.service;

import com.edunest.dto.holiday.HolidayRequest;
import com.edunest.dto.holiday.HolidayResponse;

import java.util.List;

public interface HolidayService {
    List<HolidayResponse> getHolidays(Integer tenantId);
    HolidayResponse saveHoliday(Integer tenantId, HolidayRequest request);
    boolean deleteHoliday(Integer tenantId, Integer holidayId);
}
