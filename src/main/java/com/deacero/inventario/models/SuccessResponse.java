package com.deacero.inventario.models;

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
public class SuccessResponse<T> {
	private boolean success;
	private String message;
	private T data;
	private java.time.Instant timestamp;
	private String path;

	public static <T> SuccessResponse<T> of(T data, String message, String path) {
		return SuccessResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.timestamp(java.time.Instant.now())
				.path(path)
				.build();
	}
}


