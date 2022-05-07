package com.callme.services.callservice.service;

import com.callme.services.callservice.exception.CallNotFoundException;
import com.callme.services.callservice.exception.InvalidCallActionException;
import com.callme.services.callservice.exception.UserNotFoundException;
import com.callme.services.callservice.model.Call;
import com.callme.services.callservice.model.CallRecord;
import com.callme.services.callservice.model.CallRequest;
import com.callme.services.callservice.model.CallStatus;
import com.callme.services.callservice.repository.CallRecordRepository;
import com.callme.services.callservice.repository.CallRepository;
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

    @Override
    @Transactional
    public String initiateCall(CallRequest callRequest) throws UserNotFoundException {
        // TODO validate user IDs
        // Create and store ongoing call
        Call call = new Call(callRequest.getCaller(), callRequest.getReceiver());
        callRepository.save(call);
        // TODO publish message
        return call.getId();
    }

    @Override
    @Transactional
    public void acceptCall(String callId, String handshakeInfo) throws CallNotFoundException, InvalidCallActionException {
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        call.setStatus(CallStatus.ongoing);
        callRepository.save(call);
        // TODO publish message
    }

    @Override
    @Transactional
    public void declineCall(String callId) throws CallNotFoundException, InvalidCallActionException {
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.initiating) {
            throw new InvalidCallActionException();
        }
        callRepository.delete(call);
        // TODO publish message
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
        Call call = callRepository.findById(callId)
                .orElseThrow(CallNotFoundException::new);
        if (call.getStatus() != CallStatus.ongoing) {
            throw new InvalidCallActionException();
        }
        callRepository.delete(call);
        CallRecord callRecord = new CallRecord(
                call.getCaller(),
                call.getReceiver(),
                call.getStartedAt(),
                LocalDateTime.now(),
                CallStatus.completed
        );
        callRecordRepository.save(callRecord);
    }

    @Override
    public List<CallRecord> findCallsByUserId(Long userId) throws UserNotFoundException {
        // TODO validate user IDs
        return callRecordRepository.findDistinctByCallerOrReceiver(userId, userId);
    }
}
