package com.callme.services.callservice.repository;

import com.callme.services.callservice.model.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    List<CallRecord> findDistinctByCallerOrReceiver(Long caller, Long receiver);
}
