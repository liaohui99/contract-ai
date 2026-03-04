package com.example.springaiapp.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/3/4 13:40
 * @description: TODO
 */
@Data
public class MessageReq {

    @NotNull
    private String question;

    @NotNull
    private String sessionId;

}
