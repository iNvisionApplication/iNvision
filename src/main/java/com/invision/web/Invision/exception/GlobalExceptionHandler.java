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
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
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



    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex,
                                     HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDTO(403, "FORBIDDEN",
                            "Access denied",
                            request.getRequestURI(),
                            LocalDateTime.now()));
        }
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
        return new ModelAndView("forward:/error");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public Object handleAuthorizationDenied(AuthorizationDeniedException ex,
                                            HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDTO(403, "FORBIDDEN",
                            "You do not have permission to perform this action",
                            request.getRequestURI(),
                            LocalDateTime.now()));
        }
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
        return new ModelAndView("forward:/error");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(NoResourceFoundException ex,
                                   HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.notFound().build();
        }
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        return new ModelAndView("forward:/error");
    }



    // helper method
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains("application/json"))
                || uri.startsWith("/api/");
    }
    //Generic Handler
    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception ex, HttpServletRequest request,
                                WebRequest webRequest) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO(500, "INTERNAL_SERVER_ERROR",
                            "An unexpected error occurred: " + ex.getMessage(),
                            webRequest.getDescription(false),
                            LocalDateTime.now()));
        }
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
        return new ModelAndView("forward:/error");
    }
}
