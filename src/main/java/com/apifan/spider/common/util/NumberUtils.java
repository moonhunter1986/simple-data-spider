package com.apifan.spider.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * 数字工具类
 *
 * @author yin
 */
public class NumberUtils extends org.apache.commons.lang3.math.NumberUtils {
    private static final Logger logger = LoggerFactory.getLogger(NumberUtils.class);

    /**
     * 千的后缀
     */
    private static String[] thousand_suffixes = new String[]{"k", "K", "千"};

    /**
     * 万的后缀
     */
    private static String[] ten_thousand_suffixes = new String[]{"w", "W", "万"};

    /**
     * 解析金额文本值
     *
     * @param text
     * @return 以元为单位的整数值
     */
    public static Long parseMoneyText(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        text = text.trim();
        BigDecimal multiplier;
        BigDecimal originalValue = new BigDecimal(text.substring(0, text.length() - 1));
        if (StringUtils.endsWithAny(text, thousand_suffixes)) {
            //千
            multiplier = new BigDecimal(1000);
        } else if (StringUtils.endsWithAny(text, ten_thousand_suffixes)) {
            //万
            multiplier = new BigDecimal(10000);
        } else {
            multiplier = new BigDecimal(1);
            originalValue = new BigDecimal(text);
        }
        return originalValue.multiply(multiplier).longValue();
    }


}
