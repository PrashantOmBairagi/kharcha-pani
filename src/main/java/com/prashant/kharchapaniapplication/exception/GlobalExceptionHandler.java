package com.prashant.kharchapaniapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    static final String MSG = "Message";
    static final String STATUS = "Status";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        Map<String,Object> error = new HashMap <>();
        error.put(MSG, e.getMessage());
        error.put(STATUS,404);
        return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String,Object> error = new HashMap <>();
        String message = e.getMessage();
        
        if (message != null && message.startsWith("FMONTH_REQUIRED|")) {
            String[] parts = message.split("\\|");
            if (parts.length >= 3) {
                error.put(MSG, "Financial month required for " + parts[1] + "-" + parts[2]);
                error.put("year", Integer.parseInt(parts[1]));
                error.put("month", Integer.parseInt(parts[2]));
                error.put("code", "FMONTH_REQUIRED");
                error.put(STATUS, HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
        }
        
        error.put(MSG, message);
        error.put(STATUS, HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String,Object> error = new HashMap <>();
        error.put(MSG, e.getAllErrors().get(0).getDefaultMessage());
        error.put(STATUS,HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }
}
