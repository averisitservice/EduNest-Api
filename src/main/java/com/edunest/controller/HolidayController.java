package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.holiday.HolidayRequest;
import com.edunest.entity.Holiday;
import com.edunest.service.HolidayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holiday")
public class HolidayController {

    @Autowired
    HolidayService holidayService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<List<Holiday>>> getHolidays(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<Holiday>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(holidayService.getHolidays(tenantId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Holiday>> saveHoliday(HttpServletRequest request, @RequestBody HolidayRequest holidayRequest) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Holiday> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(holidayService.saveHoliday(tenantId, holidayRequest));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{holidayId}")
    public ResponseEntity<ResponseObject<Boolean>> deleteHoliday(@PathVariable Integer holidayId) {
        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(holidayService.deleteHoliday(holidayId));
        return ResponseEntity.ok(response);
    }
}
