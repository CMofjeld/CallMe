package com.callme.services.friendservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "cannot set relationship status to that value")
public class InvalidRelationshipStatusException extends Exception {
}
