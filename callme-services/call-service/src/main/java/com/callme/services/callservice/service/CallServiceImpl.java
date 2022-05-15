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
import com.callme.services.common.service.FriendServiceClient;
import com.callme.services.common.service.StatusServiceClient;
import com.callme.services.common.service.UserServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserServiceClient userServiceClient;
    private final StatusServiceClient statusServiceClient;
    private final FriendServiceClient friendServiceClient;

    private void logDBQuery(String dbName, String queryDescription) {
        System.out.println("Querying %s to %s".formatted(dbName, queryDescription));
    }

    private void logDBStore(String dbName, String storedObject) {
        System.out.println("Storing %s in %s".formatted(storedObject, dbName));
    }

    private void logDBDelete(String dbName, String storedObject) {
        System.out.println("Deleting %s from %s".formatted(storedObject, dbName));
    }

    @Override
    @Transactional
    public String initiateCall(CallRequest callRequest) throws UserNotFoundException, InvalidCallActionException {
        // Validate user IDs
        Long caller = callRequest.getCaller();
        Long receiver = callRequest.getReceiver();
        if (!userServiceClient.userExists(caller) ||
            !userServiceClient.userExists(receiver)) {
            throw new UserNotFoundException();
        }
        // Ensure receiver is online and not busy
        UserStatusView receiverStatus = statusServiceClient.getUserStatus(receiver)
                .orElseThrow(UserNotFoundException::new);
        if (!receiverStatus.getStatus().equals("online")) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Receiver is busy or offline");
        }
        // Ensure caller and receiver are friends
        if (!friendServiceClient.areFriends(caller, receiver)) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Not friends with the receiver");
        }
        // Update user statuses
        updateStatusForUser(caller, "busy");
        updateStatusForUser(receiver, "busy");
        // Create and store ongoing call
        logDBStore("redis", "new pending call");
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
        logDBQuery("redis", "get call with ID %s".formatted(callId));
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        // Update call status
        call.setStatus(CallStatus.ongoing);
        logDBStore("redis", "updated call");
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
        Long caller = call.getCaller();
        Long receiver = call.getReceiver();
        CallRecord callRecord = new CallRecord(
                caller,
                receiver,
                call.getStartedAt(),
                LocalDateTime.now(),
                CallStatus.declined
        );
        callRecordRepository.save(callRecord);
        // Update user statuses
        updateStatusForUser(caller, "online");
        updateStatusForUser(receiver, "online");
    }

    @Override
    @Transactional
    public void disconnectCall(String callId) throws CallNotFoundException, InvalidCallActionException {
        // Validate call action
        logDBQuery("redis", "get call with ID %s".formatted(callId));
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.ongoing &&
            call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        // Remove call from the active set
        logDBDelete("redis", "call");
        callRepository.delete(call);
        // Create a new call record
        Long caller = call.getCaller();
        Long receiver = call.getReceiver();
        CallRecord callRecord = new CallRecord(
                caller,
                receiver,
                call.getStartedAt(),
                LocalDateTime.now(),
                CallStatus.completed
        );
        logDBStore("database", "call record");
        callRecordRepository.save(callRecord);
        // Publish message to receiver
        CallMessage callMessage = new CallMessage(
                caller,
                call.getId(),
                CallMessageStatus.disconnected
        );
        String topic = "calls.%d".formatted(receiver);
        publishCallMessage(callMessage, topic);
        // Publish message to caller
        callMessage = new CallMessage(
                receiver,
                call.getId(),
                CallMessageStatus.disconnected
        );
        topic = "calls.%d".formatted(caller);
        publishCallMessage(callMessage, topic);
        // Update statuses
        updateStatusForUser(caller, "online");
        updateStatusForUser(receiver, "online");
    }

    @Override
    public List<CallRecord> findCallsByUserId(Long userId) throws UserNotFoundException {
        // Validate userId
        if (!userServiceClient.userExists(userId)) {
            throw new UserNotFoundException();
        }
        logDBQuery("database", "get call records for user %d".formatted(userId));
        return callRecordRepository.findTop50ByCallerOrReceiverOrderByStartedAtDesc(userId, userId);
    }

    private void publishCallMessage(CallMessage callMessage, String topic) {
        try {
            String callMessageString = objectMapper.writeValueAsString(callMessage);
            System.out.println("Publishing call message to topic %s".formatted(topic));
            messagePublisher.publish(topic, callMessageString);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing call message to JSON.");
            System.err.println(e);
        }
    }

    private void updateStatusForUser(Long userId, String status) {
        UserStatusView callerStatus = new UserStatusView(userId, status);
        if (!statusServiceClient.setUserStatus(callerStatus)) {
            System.err.println("Failed to set status to %s for user %d".formatted(status, userId));
        }
    }
}
