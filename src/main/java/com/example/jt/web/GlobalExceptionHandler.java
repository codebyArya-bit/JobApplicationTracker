package com.example.jt.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    e.printStackTrace();
    String msg = e.getClass().getName() + ": " + (e.getMessage() != null ? e.getMessage() : "");
    return ResponseEntity.status(500).body("Server Error: " + msg);
  }
}