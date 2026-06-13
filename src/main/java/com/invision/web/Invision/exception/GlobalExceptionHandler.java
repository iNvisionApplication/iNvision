package com.invision.web.Invision.exception;

import com.invision.web.Invision.dto.ErrorResponseDTO;
import com.invision.web.Invision.exception.asset.BulkImportException;
import com.invision.web.Invision.exception.asset.DuplicateSerialNumberException;
import com.invision.web.Invision.exception.asset.ResourceNotFoundException;
import com.invision.web.Invision.exception.loan.BadLoanRequest;
import com.invision.web.Invision.exception.loan.InvalidLoanStatusChangeException;
import com.invision.web.Invision.exception.user.EmailAlreadyExistsException;
import com.invision.web.Invision.exception.user.PasswordMismatchException;
import com.invision.web.Invision.exception.user.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();


        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    //Loan Errors
    @ExceptionHandler(InvalidLoanStatusChangeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidLoanStatusChange(InvalidLoanStatusChangeException exception, WebRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                409,
                "INVALID_LOAN_STATUS_CHANGE_REQUEST",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(BadLoanRequest.class)
    public ResponseEntity<ErrorResponseDTO> handleBadLoanRequest(BadLoanRequest exception, WebRequest request){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDTO(
                400,
                "BAD_LOAN_REQUEST",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    //Asset Errors
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(DuplicateSerialNumberException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateSerialNumberException(
            DuplicateSerialNumberException ex, WebRequest request) {
        return  ResponseEntity.status(HttpStatus.CONFLICT).body( new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_SERIAL_NUMBER",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(BulkImportException.class)
    public ResponseEntity<ErrorResponseDTO> handleBulkImportException(
            BulkImportException ex, WebRequest request) {


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "BULK_IMPORT_FAILED",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }




    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {

        String message = "Database integrity violation";
        if (ex.getMessage() != null && (ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate"))) {
            message = "Duplicate entry detected. A record with this value already exists.";
        }

        return ResponseEntity.status( HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                message,
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {


        return ResponseEntity.status( HttpStatus.BAD_REQUEST).body(new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }



    //User Exceptions
    @ExceptionHandler()
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException exception, WebRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponseDTO(409,
                        "EMAIL_ALREADY EXISTS",
                        exception.getMessage(),
                        request.getDescription(false),
                        LocalDateTime.now()
                        )
        );
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handlePasswordMismatchException(
            PasswordMismatchException ex, WebRequest request) {


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "PASSWORD_MISMATCH",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(
            UserNotFoundException exception, WebRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponseDTO(404,
                        "USER_NOT_FOUND",
                        exception.getMessage(),
                        request.getDescription(false),
                        LocalDateTime.now())
        );
    }


    //Generic Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(
            Exception ex, WebRequest request) {


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        ));
    }
}
