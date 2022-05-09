package com.callme.services.callservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "receiver unavailable")
public class ReceiverUnavailableException extends Exception {
}
