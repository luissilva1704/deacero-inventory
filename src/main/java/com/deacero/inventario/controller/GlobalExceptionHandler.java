package com.deacero.inventario.controller;

import com.deacero.inventario.exception.BadRequestException;
import com.deacero.inventario.exception.InsufficientStockException;
import com.deacero.inventario.exception.ResourceNotFoundException;
import com.deacero.inventario.models.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {

	private GenericResponse<Void> build(HttpStatus status, String message, String path, String code) {
		return GenericResponse.error(message, path, status.value(), code);
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public GenericResponse<Void> handleNotFound(ResourceNotFoundException ex, ServletWebRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequest().getRequestURI(), "NOT_FOUND");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
	public GenericResponse<Void> handleBadRequest(RuntimeException ex, ServletWebRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequest().getRequestURI(), "BAD_REQUEST");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InsufficientStockException.class)
	public GenericResponse<Void> handleInsufficientStock(InsufficientStockException ex, ServletWebRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequest().getRequestURI(), "INSUFFICIENT_STOCK");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public GenericResponse<Void> handleValidation(MethodArgumentNotValidException ex, ServletWebRequest req) {
		System.out.println("Validation error: " + ex);
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + " " + f.getDefaultMessage())
				.findFirst()
				.orElse("Validation error");
		return build(HttpStatus.BAD_REQUEST, message, req.getRequest().getRequestURI(), "VALIDATION_ERROR");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public GenericResponse<Void> handleGeneric(Exception ex, ServletWebRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequest().getRequestURI(), "INTERNAL_ERROR");
	}
}


