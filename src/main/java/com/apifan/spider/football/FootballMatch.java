package com.apifan.spider.football;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 足球比赛基本信息
 *
 * @author yin
 */
public class FootballMatch implements Serializable {
    private static final long serialVersionUID = -5267882657935870222L;

    /**
     * 唯一编号
     */
    private String id;

    /**
     * 赛事名称
     */
    private String matchName;

    /**
     * 赛事附加信息(如小组赛/附加赛/回合等)
     */
    private String matchExtraInfo;

    /**
     * 主场队名
     */
    private String homeTeam;

    /**
     * 客场队名
     */
    private String awayTeam;

    /**
     * 开始时间(北京时间)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime beginTime;

    /**
     * 获取 赛事名称
     *
     * @return matchName 赛事名称
     */
    public String getMatchName() {
        return this.matchName;
    }

    /**
     * 设置 赛事名称
     *
     * @param matchName 赛事名称
     */
    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    /**
     * 获取 赛事附加信息(如小组赛附加赛回合等)
     *
     * @return matchExtraInfo 赛事附加信息(如小组赛附加赛回合等)
     */
    public String getMatchExtraInfo() {
        return this.matchExtraInfo;
    }

    /**
     * 设置 赛事附加信息(如小组赛附加赛回合等)
     *
     * @param matchExtraInfo 赛事附加信息(如小组赛附加赛回合等)
     */
    public void setMatchExtraInfo(String matchExtraInfo) {
        this.matchExtraInfo = matchExtraInfo;
    }

    /**
     * 获取 主场队名
     *
     * @return homeTeam 主场队名
     */
    public String getHomeTeam() {
        return this.homeTeam;
    }

    /**
     * 设置 主场队名
     *
     * @param homeTeam 主场队名
     */
    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    /**
     * 获取 客场队名
     *
     * @return awayTeam 客场队名
     */
    public String getAwayTeam() {
        return this.awayTeam;
    }

    /**
     * 设置 客场队名
     *
     * @param awayTeam 客场队名
     */
    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    /**
     * 获取 开始时间
     *
     * @return beginTime 开始时间
     */
    public LocalDateTime getBeginTime() {
        return this.beginTime;
    }

    /**
     * 设置 开始时间
     *
     * @param beginTime 开始时间
     */
    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * 获取 唯一编号
     *
     * @return id 唯一编号
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置 唯一编号
     *
     * @param id 唯一编号
     */
    public void setId(String id) {
        this.id = id;
    }
}
