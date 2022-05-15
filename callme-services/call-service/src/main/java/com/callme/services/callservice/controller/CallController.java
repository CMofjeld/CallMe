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

    private void logRequest(String requestAction) {
        System.out.println("Received request to " + requestAction);
    }

    private void logResponse(String responseContent) {
        System.out.println("Returning response with " + responseContent);
    }

    @PostMapping(path = "initiate")
    public ResponseEntity<String> initiateCall(
            @Valid @RequestBody CallRequest callRequest
    ) throws UserNotFoundException, InvalidCallActionException {
        logRequest("initiate a call from user %d to user %d".formatted(
                callRequest.getCaller(),
                callRequest.getReceiver()
        ));
        String callId = callService.initiateCall(callRequest);
        logResponse("call ID: %s".formatted(callId));
        return ResponseEntity.ok(
                callId
        );
    }

    @PostMapping(path = "{callId}/accept")
    public ResponseEntity<Void> initiateCall(
            @PathVariable("callId") String callId,
            @RequestBody String handshakeInfo
    ) throws CallNotFoundException, InvalidCallActionException {
        logRequest("accept call %s".formatted(callId));
        callService.acceptCall(callId, handshakeInfo);
        logResponse("status 200");
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "{callId}/decline")
    public ResponseEntity<Void> declineCall(
            @PathVariable("callId") String callId
    ) throws CallNotFoundException, InvalidCallActionException {
        logRequest("accept call %s".formatted(callId));
        callService.declineCall(callId);
        logResponse("status 200");
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "{callId}/disconnect")
    public ResponseEntity<Void> disconnectCall(
            @PathVariable("callId") String callId
    ) throws CallNotFoundException, InvalidCallActionException {
        logRequest("disconnect call %s".formatted(callId));
        callService.disconnectCall(callId);
        logResponse("status 200");
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/by/userId/{userId}")
    public ResponseEntity<List<CallRecord>> getCallsByUserId(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        logRequest("get calls for user %d".formatted(userId));
        List<CallRecord> callRecords = callService.findCallsByUserId(userId);
        logResponse("user %d's calls".formatted(userId));
        return ResponseEntity.ok(
                callRecords
        );
    }
}
