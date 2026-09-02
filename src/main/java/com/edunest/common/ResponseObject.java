package com.edunest.common;

import com.edunest.error.ErrorItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ResponseObject<T> {

    private Boolean success;
    private List<ErrorItem> errors;
    private T data;

    public ResponseObject() {
    }

    public ResponseObject(Boolean success, List<ErrorItem> errors, T data) {
        this.success = success;
        this.errors = errors;
        this.data = data;
    }

    public static <T> ResponseObject<T> success(T data) {
        return new ResponseObject<>(true, null, data);
    }

    public static <T> ResponseObject<T> error(List<ErrorItem> errors) {
        return new ResponseObject<>(false, errors, null);
    }
}
