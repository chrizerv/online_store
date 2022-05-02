package com.mythesis.eshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public  ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        System.out.println(sw);
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return buildErrorResponse(ex,apiError,HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public  ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {

        List<String> violations = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(),violations);
        apiError.setMessage("Fields validation error");
        return buildErrorResponse(ex,apiError,HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public  ResponseEntity<ApiError> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        System.out.println(sw.toString());
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(),ex.getMessage());

        return buildErrorResponse(ex,apiError,HttpStatus.NOT_FOUND, request);
    }




    private ResponseEntity<ApiError> buildErrorResponse(Exception ex,
                                                        ApiError bodyError,
                                                        HttpStatus httpStatus,
                                                        WebRequest request){

        return ResponseEntity.status(httpStatus).body(bodyError);
    }

}

