package com.workhub.global.clientInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CllientInfoDto {

    private String ipAddress;
    private String userAgent;
}
