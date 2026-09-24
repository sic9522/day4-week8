package com.example.day4_week8.payloads.response;

import java.time.LocalDateTime;

import lombok.Getter;

// Un solo formato di errore per tutta l'API: qualunque cosa vada storta, il
// frontend legge sempre le stesse chiavi.
@Getter
public class ErrorResponse {

	private final String message;
	private final LocalDateTime timestamp;

	public ErrorResponse(String message) {
		this.message = message;
		this.timestamp = LocalDateTime.now();
	}

}
