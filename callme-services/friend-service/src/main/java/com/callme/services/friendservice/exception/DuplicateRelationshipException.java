package com.callme.services.friendservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "relationship between users already exists")
public class DuplicateRelationshipException extends Exception {
}
