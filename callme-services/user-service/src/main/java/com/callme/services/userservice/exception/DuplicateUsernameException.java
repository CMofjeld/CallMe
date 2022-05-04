package com.callme.services.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "user with that username already exists")
public class DuplicateUsernameException extends RuntimeException {
}
