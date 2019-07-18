package com.apifan.spider.fund;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 开放式基金信息
 *
 * @author yin
 */
public class OpenEndFund implements Serializable {
    private static final long serialVersionUID = 1343199241145905518L;

    /**
     * 交易日(yyyyMMdd)
     */
    private Integer ocDate;

    /**
     * 基金代码
     */
    private String fundCode;

    /**
     * 基金名称
     */
    private String fundName;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 单位净值
     */
    private BigDecimal netAssetValue;

    /**
     * 累计净值
     */
    private BigDecimal netAccumValue;

    /**
     * 日增长
     */
    private BigDecimal increment;

    /**
     * 日增长率
     */
    private BigDecimal incrementRate;

    /**
     * 申购状态
     */
    private String purchaseStatus;

    /**
     * 赎回状态
     */
    private String redemptionStatus;

    /**
     * 手续费率
     */
    private BigDecimal redemptionFeeRate;

    /**
     * 获取 交易日(yyyyMMdd)
     *
     * @return ocDate 交易日(yyyyMMdd)
     */
    public Integer getOcDate() {
        return this.ocDate;
    }

    /**
     * 设置 交易日(yyyyMMdd)
     *
     * @param ocDate 交易日(yyyyMMdd)
     */
    public void setOcDate(Integer ocDate) {
        this.ocDate = ocDate;
    }

    /**
     * 获取 基金代码
     *
     * @return fundCode 基金代码
     */
    public String getFundCode() {
        return this.fundCode;
    }

    /**
     * 设置 基金代码
     *
     * @param fundCode 基金代码
     */
    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    /**
     * 获取 基金名称
     *
     * @return fundName 基金名称
     */
    public String getFundName() {
        return this.fundName;
    }

    /**
     * 设置 基金名称
     *
     * @param fundName 基金名称
     */
    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    /**
     * 获取 单位净值
     *
     * @return netAssetValue 单位净值
     */
    public BigDecimal getNetAssetValue() {
        return this.netAssetValue;
    }

    /**
     * 设置 单位净值
     *
     * @param netAssetValue 单位净值
     */
    public void setNetAssetValue(BigDecimal netAssetValue) {
        this.netAssetValue = netAssetValue;
    }

    /**
     * 获取 累计净值
     *
     * @return netAccumValue 累计净值
     */
    public BigDecimal getNetAccumValue() {
        return this.netAccumValue;
    }

    /**
     * 设置 累计净值
     *
     * @param netAccumValue 累计净值
     */
    public void setNetAccumValue(BigDecimal netAccumValue) {
        this.netAccumValue = netAccumValue;
    }

    /**
     * 获取 日增长
     *
     * @return increment 日增长
     */
    public BigDecimal getIncrement() {
        return this.increment;
    }

    /**
     * 设置 日增长
     *
     * @param increment 日增长
     */
    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    /**
     * 获取 日增长率
     *
     * @return incrementRate 日增长率
     */
    public BigDecimal getIncrementRate() {
        return this.incrementRate;
    }

    /**
     * 设置 日增长率
     *
     * @param incrementRate 日增长率
     */
    public void setIncrementRate(BigDecimal incrementRate) {
        this.incrementRate = incrementRate;
    }

    /**
     * 获取 申购状态
     *
     * @return purchaseStatus 申购状态
     */
    public String getPurchaseStatus() {
        return this.purchaseStatus;
    }

    /**
     * 设置 申购状态
     *
     * @param purchaseStatus 申购状态
     */
    public void setPurchaseStatus(String purchaseStatus) {
        this.purchaseStatus = purchaseStatus;
    }

    /**
     * 获取 赎回状态
     *
     * @return redemptionStatus 赎回状态
     */
    public String getRedemptionStatus() {
        return this.redemptionStatus;
    }

    /**
     * 设置 赎回状态
     *
     * @param redemptionStatus 赎回状态
     */
    public void setRedemptionStatus(String redemptionStatus) {
        this.redemptionStatus = redemptionStatus;
    }

    /**
     * 获取 手续费率
     *
     * @return redemptionFeeRate 手续费率
     */
    public BigDecimal getRedemptionFeeRate() {
        return this.redemptionFeeRate;
    }

    /**
     * 设置 手续费率
     *
     * @param redemptionFeeRate 手续费率
     */
    public void setRedemptionFeeRate(BigDecimal redemptionFeeRate) {
        this.redemptionFeeRate = redemptionFeeRate;
    }

    /**
     * 获取 拼音
     *
     * @return pinyin 拼音
     */
    public String getPinyin() {
        return this.pinyin;
    }

    /**
     * 设置 拼音
     *
     * @param pinyin 拼音
     */
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
