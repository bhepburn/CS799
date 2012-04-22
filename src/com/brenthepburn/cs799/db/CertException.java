package com.brenthepburn.cs799.db;

public class CertException extends Throwable {
	private static final long serialVersionUID = 1L;

	private String message;

	public CertException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
