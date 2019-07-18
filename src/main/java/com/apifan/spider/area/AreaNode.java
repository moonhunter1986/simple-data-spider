package com.apifan.spider.area;

import java.io.Serializable;

/**
 * 行政区划信息
 *
 * @author yin
 */
public class AreaNode implements Serializable {
    private static final long serialVersionUID = -6709610618285703456L;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 所属上级节点的编码(0为顶级节点)
     */
    private String parentCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
