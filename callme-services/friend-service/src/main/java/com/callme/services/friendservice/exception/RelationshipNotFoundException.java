package com.callme.services.friendservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "relationship not found")
public class RelationshipNotFoundException extends Exception {
}
