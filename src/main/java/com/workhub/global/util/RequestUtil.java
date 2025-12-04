package com.workhub.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
@Deprecated (forRemoval = true, since = "SecurityUtil에서 같은 기능을 사용할 수 있습니다.")
@Component
public class RequestUtil {

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }
}
