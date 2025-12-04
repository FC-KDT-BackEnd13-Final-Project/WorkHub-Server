package com.workhub.global.clientInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Deprecated
@Getter
@AllArgsConstructor
public class ClientInfoDto {

    private String ipAddress;
    private String userAgent;
}
