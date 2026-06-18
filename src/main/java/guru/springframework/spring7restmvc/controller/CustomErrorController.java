package guru.springframework.spring7restmvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class CustomErrorController {
 
    @ExceptionHandler(TransactionSystemException.class)
    ResponseEntity handleJPAViolations(TransactionSystemException exception){
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();
        if(exception.getCause() != null
          && exception.getCause().getCause() instanceof ConstraintViolationException){
            ConstraintViolationException vExc = (ConstraintViolationException) exception.getCause().getCause();
            List errorList = vExc.getConstraintViolations()
                    .stream()
                    .map(constraintViolation -> {
                        Map<String, String> errorMap = new HashMap<>();
                        errorMap.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
                        return errorMap;
                    }).collect(Collectors.toList());
            return responseEntity.body(errorList);
        }
        return responseEntity.build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity handleBindErrors(MethodArgumentNotValidException exception){
        List errorList = exception.getFieldErrors()
                .stream()
                .map(fieldError -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    return errorMap;
                }).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errorList);
    }
}
