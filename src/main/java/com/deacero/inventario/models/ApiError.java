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
public class ApiError {
	private java.time.Instant timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	private String code;
}


