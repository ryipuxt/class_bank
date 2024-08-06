package com.tenco.bank.handler.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class RedirectException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HttpStatus status;

	// throw new RedirectException(???, ???);
	public RedirectException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

}
