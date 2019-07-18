package com.apifan.spider.mobile;

import java.io.Serializable;

/**
 * 手机号段归属地信息
 *
 * @author yin
 */
public class MobileLocation implements Serializable {
    private static final long serialVersionUID = 4316930307167488891L;

    /**
     * 手机号码前7位
     */
    private String mobilePrefix;

    /**
     * 省/直辖市/自治区
     */
    private String province;

    /**
     * 地市
     */
    private String city;

    /**
     * 运营商名称
     */
    private String carrierName;

    public String getMobilePrefix() {
        return mobilePrefix;
    }

    public void setMobilePrefix(String mobilePrefix) {
        this.mobilePrefix = mobilePrefix;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }
}
