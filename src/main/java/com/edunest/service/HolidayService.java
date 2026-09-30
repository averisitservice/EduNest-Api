package com.edunest.service;

import com.edunest.dto.holiday.HolidayRequest;
import com.edunest.entity.Holiday;

import java.util.List;

public interface HolidayService {
    List<Holiday> getHolidays(Integer tenantId);

    Holiday saveHoliday(Integer tenantId, HolidayRequest request);

    boolean deleteHoliday(Integer holidayId);
}
