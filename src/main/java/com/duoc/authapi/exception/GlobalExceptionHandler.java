package com.duoc.authapi.exception;

import com.amazonaws.services.cognitoidp.model.*;
import com.duoc.authapi.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Errores propios de negocio (ej: email duplicado en MySQL) ---
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- Validación de @Valid en los DTOs ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        return build(HttpStatus.BAD_REQUEST, mensaje);
    }

    // --- Errores específicos que devuelve Cognito ---

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserExists(UsernameExistsException ex) {
        return build(HttpStatus.CONFLICT, "Ya existe un usuario con ese email en Cognito");
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAuthorized(NotAuthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }

    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ApiErrorResponse> handleNotConfirmed(UserNotConfirmedException ex) {
        return build(HttpStatus.FORBIDDEN, "Debes confirmar tu email antes de iniciar sesión");
    }

    @ExceptionHandler(CodeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleCodeMismatch(CodeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "El código de confirmación es incorrecto");
    }

    @ExceptionHandler(ExpiredCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredCode(ExpiredCodeException ex) {
        return build(HttpStatus.BAD_REQUEST, "El código de confirmación expiró, solicita uno nuevo");
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return build(HttpStatus.BAD_REQUEST, "La contraseña no cumple la política de seguridad de Cognito");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }

    // --- Cualquier otro error no contemplado ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String mensaje) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje
        );
        return ResponseEntity.status(status).body(body);
    }
}
