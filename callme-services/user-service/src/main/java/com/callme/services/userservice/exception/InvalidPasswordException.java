package com.callme.services.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "invalid password")
public class InvalidPasswordException extends RuntimeException {
}
