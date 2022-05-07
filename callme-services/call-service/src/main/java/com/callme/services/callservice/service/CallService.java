package com.callme.services.callservice.service;

import com.callme.services.callservice.exception.CallNotFoundException;
import com.callme.services.callservice.exception.InvalidCallActionException;
import com.callme.services.callservice.exception.UserNotFoundException;
import com.callme.services.callservice.model.CallRecord;
import com.callme.services.callservice.model.CallRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CallService {
    public String initiateCall(CallRequest callRequest) throws UserNotFoundException;
    public void acceptCall(String callId, String handshakeInfo) throws CallNotFoundException, InvalidCallActionException;
    public void declineCall(String callId) throws CallNotFoundException, InvalidCallActionException;
    public void disconnectCall(String callId) throws CallNotFoundException, InvalidCallActionException;
    public List<CallRecord> findCallsByUserId(Long userId) throws UserNotFoundException;
}
