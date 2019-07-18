package com.apifan.spider.lagou;

import java.io.Serializable;

/**
 * 拉勾网职位信息
 *
 * @author yinzhili
 * @date 2019-07-13
 * @since
 */
public class LagouPositionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 职位ID
     */
    private String positionId;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 薪水范围
     */
    private String salaryRange;

    /**
     * 薪水范围起点(元)
     */
    private Long salaryBegin;

    /**
     * 薪水范围终点(元)
     */
    private Long salaryEnd;

    /**
     * 公司ID
     */
    private String companyId;

    /**
     * 公司全名
     */
    private String companyFullName;

    /**
     * 职位创建日期(格式:yyyyMMdd)
     */
    private String createDate;

    /**
     * 获取 职位ID
     *
     * @return positionId 职位ID
     */
    public String getPositionId() {
        return this.positionId;
    }

    /**
     * 设置 职位ID
     *
     * @param positionId 职位ID
     */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    /**
     * 获取 职位名称
     *
     * @return positionName 职位名称
     */
    public String getPositionName() {
        return this.positionName;
    }

    /**
     * 设置 职位名称
     *
     * @param positionName 职位名称
     */
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    /**
     * 获取 所在城市
     *
     * @return city 所在城市
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置 所在城市
     *
     * @param city 所在城市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取 公司ID
     *
     * @return companyId 公司ID
     */
    public String getCompanyId() {
        return this.companyId;
    }

    /**
     * 设置 公司ID
     *
     * @param companyId 公司ID
     */
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    /**
     * 获取 公司全名
     *
     * @return companyFullName 公司全名
     */
    public String getCompanyFullName() {
        return this.companyFullName;
    }

    /**
     * 设置 公司全名
     *
     * @param companyFullName 公司全名
     */
    public void setCompanyFullName(String companyFullName) {
        this.companyFullName = companyFullName;
    }

    /**
     * 获取 薪水范围
     *
     * @return salaryRange 薪水范围
     */
    public String getSalaryRange() {
        return this.salaryRange;
    }

    /**
     * 设置 薪水范围
     *
     * @param salaryRange 薪水范围
     */
    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    /**
     * 获取 薪水范围起点(元)
     *
     * @return salaryBegin 薪水范围起点(元)
     */
    public Long getSalaryBegin() {
        return this.salaryBegin;
    }

    /**
     * 设置 薪水范围起点(元)
     *
     * @param salaryBegin 薪水范围起点(元)
     */
    public void setSalaryBegin(Long salaryBegin) {
        this.salaryBegin = salaryBegin;
    }

    /**
     * 获取 薪水范围终点(元)
     *
     * @return salaryEnd 薪水范围终点(元)
     */
    public Long getSalaryEnd() {
        return this.salaryEnd;
    }

    /**
     * 设置 薪水范围终点(元)
     *
     * @param salaryEnd 薪水范围终点(元)
     */
    public void setSalaryEnd(Long salaryEnd) {
        this.salaryEnd = salaryEnd;
    }

    /**
     * 获取 职位创建日期
     *
     * @return createDate 职位创建日期
     */
    public String getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 职位创建日期
     *
     * @param createDate 职位创建日期
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
