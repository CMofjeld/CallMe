package com.example.statusservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@RedisHash("UserStatus")
public class UserStatus implements Serializable {
    @NotBlank
    @Id
    private String id;
    @NotBlank
    private String status;
    private Long updatedAt;

    public UserStatus(String id, String status) {
        this.id = id;
        this.status = status;
        this.updatedAt = new Date().getTime();
    }
}
