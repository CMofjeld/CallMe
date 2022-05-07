package com.callme.services.callservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallRecord {
    @Id
    @SequenceGenerator(
            name = "call_sequence",
            sequenceName = "call_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "call_sequence"
    )
    private Long id;
    @NotNull
    private Long caller;
    @NotNull
    private Long receiver;
    @NotNull
    private LocalDateTime startedAt;
    @NotNull
    private LocalDateTime completedAt;
    @NotNull
    private CallStatus status;

    public CallRecord(Long caller, Long receiver, LocalDateTime startedAt, LocalDateTime completedAt, CallStatus status) {
        this.caller = caller;
        this.receiver = receiver;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
    }
}
