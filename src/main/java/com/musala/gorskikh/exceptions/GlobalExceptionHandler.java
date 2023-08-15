package com.musala.gorskikh.exceptions;

import com.musala.gorskikh.model.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice(basePackages = "com.musala.gorskikh")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Error> globalHandler(Exception ex, WebRequest request) {
        log.error("Occurred exception. Request: {}", request.getDescription(false), ex);

        return ResponseEntity.internalServerError()
                .body(createError(INTERNAL_SERVER_ERROR, ex));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class,
            BindException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    private ResponseEntity<Error> badRequestHandler(Exception ex, WebRequest request) {
        log.error("Occurred bad request exception. Request: {}", request.getDescription(false), ex);

        return ResponseEntity.internalServerError()
                .body(createError(BAD_REQUEST, ex));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    private ResponseEntity<Error> httpServerErrorHandler(HttpServerErrorException ex, WebRequest request) {
        log.error("Occurred http request exception. Request: {}", request.getDescription(false), ex);

        return ResponseEntity.internalServerError()
                .body(createError((HttpStatus)ex.getStatusCode(), ex));
    }

    private static Error createError(HttpStatus status, Exception ex) {
        return new Error()
                .code(status.value())
                .description(ex.getMessage())
                .message(status.getReasonPhrase());
    }
}
