package com.deacero.inventario.controller;

import com.deacero.inventario.exception.BadRequestException;
import com.deacero.inventario.exception.InsufficientStockException;
import com.deacero.inventario.exception.ResourceNotFoundException;
import com.deacero.inventario.models.GenericResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

	@Test
	void mapsNotFound() {
		GlobalExceptionHandler h = new GlobalExceptionHandler();
		ServletWebRequest req = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);
		when(req.getRequest().getRequestURI()).thenReturn("/x");
		GenericResponse<Void> r = h.handleNotFound(new ResourceNotFoundException("nf"), req);
		assertEquals("NOT_FOUND", r.getCode());
		assertEquals(404, r.getStatus());
	}

	@Test
	void mapsBadRequest() {
		GlobalExceptionHandler h = new GlobalExceptionHandler();
		ServletWebRequest req = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);
		when(req.getRequest().getRequestURI()).thenReturn("/x");
		GenericResponse<Void> r = h.handleBadRequest(new BadRequestException("b"), req);
		assertEquals("BAD_REQUEST", r.getCode());
		assertEquals(400, r.getStatus());
	}

	@Test
	void mapsInsufficient() {
		GlobalExceptionHandler h = new GlobalExceptionHandler();
		ServletWebRequest req = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);
		when(req.getRequest().getRequestURI()).thenReturn("/x");
		GenericResponse<Void> r = h.handleInsufficientStock(new InsufficientStockException("i"), req);
		assertEquals("INSUFFICIENT_STOCK", r.getCode());
		assertEquals(400, r.getStatus());
	}

	@Test
	void mapsGeneric() {
		GlobalExceptionHandler h = new GlobalExceptionHandler();
		ServletWebRequest req = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);
		when(req.getRequest().getRequestURI()).thenReturn("/x");
		GenericResponse<Void> r = h.handleGeneric(new RuntimeException("e"), req);
		assertEquals("INTERNAL_ERROR", r.getCode());
		assertEquals(500, r.getStatus());
	}

	@Test
	void mapsValidation() {
		GlobalExceptionHandler h = new GlobalExceptionHandler();
		ServletWebRequest req = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);
		when(req.getRequest().getRequestURI()).thenReturn("/x");
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(new FieldError("productRequest", "sku", "must not be blank")));
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		when(ex.getBindingResult()).thenReturn(bindingResult);
		GenericResponse<Void> r = h.handleValidation(ex, req);
		assertEquals("VALIDATION_ERROR", r.getCode());
		assertEquals(400, r.getStatus());
	}
}


