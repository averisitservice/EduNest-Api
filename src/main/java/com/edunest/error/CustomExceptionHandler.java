package com.edunest.error;

import com.edunest.common.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class CustomExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseObject<Object>> handleCustomException(CustomException ex) {

        ResponseObject<Object> response = new ResponseObject<>();
        response.setSuccess(false);
        response.setData(null);
        response.setErrors(Collections.singletonList(new ErrorItem(ex.getParam(), ex.getMsg())));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject<Object>> handleGenericException(Exception ex) {

        LOG.error("Unhandled exception", ex);

        ResponseObject<Object> response = new ResponseObject<>();
        response.setSuccess(false);
        response.setData(null);
        response.setErrors(Collections.singletonList(new ErrorItem("server", "Something went wrong. Please try again later.")));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}