package com.callme.services.callservice.controller;

import com.callme.services.callservice.exception.CallNotFoundException;
import com.callme.services.callservice.exception.InvalidCallActionException;
import com.callme.services.callservice.exception.UserNotFoundException;
import com.callme.services.callservice.model.CallRecord;
import com.callme.services.callservice.model.CallRequest;
import com.callme.services.callservice.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(path = "calls")
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    @PostMapping(path = "initiate")
    public ResponseEntity<String> initiateCall(
            @Valid @RequestBody CallRequest callRequest
    ) throws UserNotFoundException, InvalidCallActionException {
        return ResponseEntity.ok(
                callService.initiateCall(callRequest)
        );
    }

    @PostMapping(path = "{callId}/accept")
    public ResponseEntity<Void> initiateCall(
            @PathVariable("callId") String callId,
            @RequestBody String handshakeInfo
    ) throws CallNotFoundException, InvalidCallActionException {
        callService.acceptCall(callId, handshakeInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "{callId}/decline")
    public ResponseEntity<Void> declineCall(
            @PathVariable("callId") String callId
    ) throws CallNotFoundException, InvalidCallActionException {
        callService.declineCall(callId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "{callId}/disconnect")
    public ResponseEntity<Void> disconnectCall(
            @PathVariable("callId") String callId
    ) throws CallNotFoundException, InvalidCallActionException {
        callService.disconnectCall(callId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/by/userId/{userId}")
    public ResponseEntity<List<CallRecord>> getCallsByUserId(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        return ResponseEntity.ok(
                callService.findCallsByUserId(userId)
        );
    }
}
