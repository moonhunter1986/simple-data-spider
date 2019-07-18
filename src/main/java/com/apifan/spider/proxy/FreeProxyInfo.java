package com.apifan.spider.proxy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 免费代理基本信息
 *
 * @author yin
 */
public class FreeProxyInfo implements Serializable {
    private static final long serialVersionUID = -9100288382190286372L;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口号
     */
    private int port;

    /**
     * 是否匿名
     */
    private String isAnonymous;

    /**
     * 类型
     */
    private String type;

    /**
     * 连接速度(秒)
     */
    private BigDecimal speed;

    /**
     * 存活时间(分钟)
     */
    private int surviveMinutes;

    /**
     * 最近验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 获取 IP
     *
     * @return ip IP
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * 设置 IP
     *
     * @param ip IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 获取 端口号
     *
     * @return port 端口号
     */
    public int getPort() {
        return this.port;
    }

    /**
     * 设置 端口号
     *
     * @param port 端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取 是否匿名
     *
     * @return isAnonymous 是否匿名
     */
    public String getIsAnonymous() {
        return this.isAnonymous;
    }

    /**
     * 设置 是否匿名
     *
     * @param isAnonymous 是否匿名
     */
    public void setIsAnonymous(String isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    /**
     * 获取 类型
     *
     * @return type 类型
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置 类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取 连接速度
     *
     * @return speed 连接速度
     */
    public BigDecimal getSpeed() {
        return this.speed;
    }

    /**
     * 设置 连接速度
     *
     * @param speed 连接速度
     */
    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    /**
     * 获取 存活时间(分钟)
     *
     * @return surviveMinutes 存活时间(分钟)
     */
    public int getSurviveMinutes() {
        return this.surviveMinutes;
    }

    /**
     * 设置 存活时间(分钟)
     *
     * @param surviveMinutes 存活时间(分钟)
     */
    public void setSurviveMinutes(int surviveMinutes) {
        this.surviveMinutes = surviveMinutes;
    }

    /**
     * 获取 最近验证时间
     *
     * @return verifyTime 最近验证时间
     */
    public LocalDateTime getVerifyTime() {
        return this.verifyTime;
    }

    /**
     * 设置 最近验证时间
     *
     * @param verifyTime 最近验证时间
     */
    public void setVerifyTime(LocalDateTime verifyTime) {
        this.verifyTime = verifyTime;
    }
}
