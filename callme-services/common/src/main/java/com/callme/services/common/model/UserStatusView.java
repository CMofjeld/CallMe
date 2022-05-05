package com.callme.services.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class UserStatusView {
    @NotNull
    private Long id;
    @NotBlank
    private String status;
}
