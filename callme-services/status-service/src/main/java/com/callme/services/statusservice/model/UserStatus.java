package com.callme.services.statusservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@RedisHash("UserStatus")
public class UserStatus implements Serializable {
    @NotNull
    @Id
    private Long id;
    @NotBlank
    private String status;
    private Long updatedAt;

    public UserStatus(Long id, String status) {
        this.id = id;
        this.status = status;
        this.updatedAt = new Date().getTime();
    }
}
