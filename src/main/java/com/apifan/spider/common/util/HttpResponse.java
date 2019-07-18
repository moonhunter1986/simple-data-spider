package com.apifan.spider.common.util;

import java.io.Serializable;
import java.util.Map;

/**
 * HTTP请求响应
 *
 * @author yin
 */
public class HttpResponse implements Serializable {
    private static final long serialVersionUID = -5475690197719799772L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应文本
     */
    private String message;

    /**
     * 响应headers
     */
    private Map<String, String> headers;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
