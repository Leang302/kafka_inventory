package com.leang.orderservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {
    private ResponseEntity<ProblemDetail> problemDetailResponseEntity(Map<?, ?> errors, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        if (title != null) {
            problemDetail.setTitle(title);
        }
        problemDetail.setProperty("code", HttpStatus.BAD_REQUEST.value());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("error", errors);
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ProblemDetail> problemDetailResponseEntity(String error, HttpStatus status, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        if (title != null) {
            problemDetail.setTitle(title);
        }
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setDetail(error);
        return new ResponseEntity<>(problemDetail, status);
    }

    private ResponseEntity<ProblemDetail> problemDetailResponseEntity(String error, HttpStatus status) {
        return problemDetailResponseEntity(error, status, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return problemDetailResponseEntity(errors, "Method Field Validation Failed");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handlerMethodValidationException(HandlerMethodValidationException e) {
        Map<String, String> errors = new HashMap<>();

        // Loop through each invalid parameter validation unit
        e.getParameterValidationResults().forEach(paramaterError -> {
            String parameterName = paramaterError.getMethodParameter().getParameterName();

            for (MessageSourceResolvable errorMessage : paramaterError.getResolvableErrors()) {
                errors.put(parameterName, errorMessage.getDefaultMessage());
            }
        });

        return problemDetailResponseEntity(errors, "Method Parameter Validation Failed");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFoundException(NotFoundException e) {
        return problemDetailResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> badRequestException(BadRequestException e) {
        return problemDetailResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    // Handle all unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllUnhandledExceptions(Exception e) {
        logger.error("Unhandled exception occurred", e);
        StackTraceElement[] stack = e.getStackTrace();
        String errorMsg = "";
        if (stack.length > 0) {
            StackTraceElement origin = stack[0];
            errorMsg = e.getClass().getSimpleName() + " at " + origin.getClassName() + "."
                    + origin.getMethodName() + "(" + origin.getFileName() + ":"
                    + origin.getLineNumber() + ")";
        } else {
            errorMsg = e.getClass().getSimpleName();
        }

        return problemDetailResponseEntity(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
