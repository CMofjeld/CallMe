package com.callme.services.callservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallRequest {
    @NotNull
    private Long caller;
    @NotNull
    private Long receiver;
    @NotNull
    private String handshakeInfo;
}
