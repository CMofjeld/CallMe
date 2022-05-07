package com.callme.services.callservice.service;

import com.callme.services.callservice.exception.CallNotFoundException;
import com.callme.services.callservice.exception.InvalidCallActionException;
import com.callme.services.callservice.exception.UserNotFoundException;
import com.callme.services.callservice.messaging.CallMessage;
import com.callme.services.callservice.messaging.CallMessageStatus;
import com.callme.services.callservice.messaging.MessagePublisher;
import com.callme.services.callservice.model.Call;
import com.callme.services.callservice.model.CallRecord;
import com.callme.services.callservice.model.CallRequest;
import com.callme.services.callservice.model.CallStatus;
import com.callme.services.callservice.repository.CallRecordRepository;
import com.callme.services.callservice.repository.CallRepository;
import com.callme.services.common.model.UserStatusView;
import com.callme.services.common.service.UserServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallServiceImpl implements CallService {
    private final CallRepository callRepository;
    private final CallRecordRepository callRecordRepository;
    private final MessagePublisher messagePublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public String initiateCall(CallRequest callRequest) throws UserNotFoundException {
        // TODO validate user IDs and friend status
        // Create and store ongoing call
        Call call = new Call(callRequest.getCaller(), callRequest.getReceiver());
        callRepository.save(call);
        // Publish message to receiver
        CallMessage callMessage = new CallMessage(
                call.getCaller(),
                call.getId(),
                CallMessageStatus.offering,
                callRequest.getHandshakeInfo()
        );
        String topic = "calls.%d".formatted(call.getReceiver());
        publishCallMessage(callMessage, topic);
        // Return call ID to caller
        return call.getId();
    }

    @Override
    @Transactional
    public void acceptCall(String callId, String handshakeInfo) throws CallNotFoundException, InvalidCallActionException {
        // Validate call action
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        // Update call status
        call.setStatus(CallStatus.ongoing);
        callRepository.save(call);
        // Publish message to caller
        CallMessage callMessage = new CallMessage(
                call.getReceiver(),
                call.getId(),
                CallMessageStatus.accepted,
                handshakeInfo
        );
        String topic = "calls.%d".formatted(call.getCaller());
        publishCallMessage(callMessage, topic);
    }

    @Override
    @Transactional
    public void declineCall(String callId) throws CallNotFoundException, InvalidCallActionException {
        // Validate call action
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        // Remove the call from the active set
        callRepository.delete(call);
        // Publish message to caller
        CallMessage callMessage = new CallMessage(
                call.getReceiver(),
                call.getId(),
                CallMessageStatus.declined
        );
        String topic = "calls.%d".formatted(call.getCaller());
        publishCallMessage(callMessage, topic);
        // Create new call record
        CallRecord callRecord = new CallRecord(
                call.getCaller(),
                call.getReceiver(),
                call.getStartedAt(),
                LocalDateTime.now(),
                CallStatus.declined
        );
        callRecordRepository.save(callRecord);
    }

    @Override
    @Transactional
    public void disconnectCall(String callId) throws CallNotFoundException, InvalidCallActionException {
        // Validate call action
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.ongoing &&
            call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        // Remove call from the active set
        callRepository.delete(call);
        // Create a new call record
        CallRecord callRecord = new CallRecord(
                call.getCaller(),
                call.getReceiver(),
                call.getStartedAt(),
                LocalDateTime.now(),
                CallStatus.completed
        );
        callRecordRepository.save(callRecord);
        // Publish message to receiver, if necessary
        if (call.getStatus() == CallStatus.initiating) {
            CallMessage callMessage = new CallMessage(
                    call.getCaller(),
                    call.getId(),
                    CallMessageStatus.disconnected
            );
            String topic = "calls.%d".formatted(call.getReceiver());
            publishCallMessage(callMessage, topic);
        }
    }

    @Override
    public List<CallRecord> findCallsByUserId(Long userId) throws UserNotFoundException {
        // TODO validate user IDs
        return callRecordRepository.findDistinctByCallerOrReceiver(userId, userId);
    }

    private void publishCallMessage(CallMessage callMessage, String topic) {
        try {
            String callMessageString = objectMapper.writeValueAsString(callMessage);
            messagePublisher.publish(topic, callMessageString);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing call message to JSON.");
            System.err.println(e);
        }
    }
}
