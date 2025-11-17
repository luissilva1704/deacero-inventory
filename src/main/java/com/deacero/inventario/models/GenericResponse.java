package com.deacero.inventario.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {
	private boolean success;
	private String message;
	private T data;
	private java.time.Instant timestamp;
	private String path;
	private Integer status;
	private String code;

	public static <T> GenericResponse<T> ok(T data, String message, String path) {
		return GenericResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.timestamp(java.time.Instant.now())
				.path(path)
				.status(200)
				.build();
	}

	public static GenericResponse<Void> error(String message, String path, int status, String code) {
		return GenericResponse.<Void>builder()
				.success(false)
				.message(message)
				.data(null)
				.timestamp(java.time.Instant.now())
				.path(path)
				.status(status)
				.code(code)
				.build();
	}
}


