package com.edunest.service;

import com.edunest.dto.holiday.HolidayRequest;
import com.edunest.dto.holiday.HolidayResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Holiday;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.HolidayRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private CommonHelper commonHelper;

    @Override
    public List<HolidayResponse> getHolidays(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        List<Holiday> holidays = holidayRepository.findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByStartDateAsc(
                tenantId, currentYear.getAcademicYearId());

        List<HolidayResponse> responses = new ArrayList<>();
        for (Holiday holiday : holidays) {
            HolidayResponse res = new HolidayResponse();
            BeanUtils.copyProperties(holiday, res);
            responses.add(res);
        }
        return responses;
    }

    @Override
    public HolidayResponse saveHoliday(Integer tenantId, HolidayRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        Holiday holiday;
        if (request.getHolidayId() != null) {
            holiday = holidayRepository.findById(request.getHolidayId())
                    .orElseThrow(() -> new CustomException("holidayId", "Holiday not found"));
        } else {
            holiday = new Holiday();
            holiday.setTenantId(tenantId);
            holiday.setAcademicYearId(currentYear.getAcademicYearId());
            holiday.setIsActive(true);
        }

        holiday.setHolidayName(request.getHolidayName());
        holiday.setStartDate(request.getStartDate());
        holiday.setEndDate(request.getEndDate() != null ? request.getEndDate() : request.getStartDate());
        holiday.setHolidayType(request.getHolidayType());
        holiday.setDescription(request.getDescription());

        Holiday saved = holidayRepository.save(holiday);

        HolidayResponse response = new HolidayResponse();
        BeanUtils.copyProperties(saved, response);
        return response;
    }

    @Override
    public boolean deleteHoliday(Integer tenantId, Integer holidayId) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new CustomException("holidayId", "Holiday not found"));
        holiday.setIsActive(false);
        holidayRepository.save(holiday);
        return true;
    }
}
