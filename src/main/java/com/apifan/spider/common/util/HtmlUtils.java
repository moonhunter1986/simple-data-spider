package com.apifan.spider.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * HTML工具类
 *
 * @author yin
 */
public class HtmlUtils {

    /**
     * 删除链接中的html标签以及特殊字符
     *
     * @param htmlStr
     * @return
     */
    public static String getSafeText(String htmlStr) {
        if (StringUtils.isEmpty(htmlStr)) {
            return null;
        }
        //去掉脚本标签
        String scriptRegex = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        //去掉样式标签
        String styleRegex = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        //去掉html标签
        String htmlRegex = "<[^>]+>";
        //去掉空格
        String spaceRegex = "\\s*|\t|\r|\n";
        htmlStr = htmlStr.replaceAll(scriptRegex, "");
        htmlStr = htmlStr.replaceAll(styleRegex, "");
        htmlStr = htmlStr.replaceAll(htmlRegex, "");
        htmlStr = htmlStr.replaceAll(spaceRegex, "");
        //去掉正反斜杠
        htmlStr = htmlStr.replaceAll("\\\\", "");
        htmlStr = htmlStr.replaceAll("/", "");
        //去掉竖线
        htmlStr = htmlStr.replaceAll("\\|", "");
        //去掉双引号
        htmlStr = htmlStr.replaceAll("\"", "");
        //去掉单引号
        htmlStr = htmlStr.replaceAll("'", "");
        //去掉问号
        htmlStr = htmlStr.replaceAll("\\?", "");
        //去掉冒号
        htmlStr = htmlStr.replaceAll(":", "");
        return StringUtils.trim(htmlStr);
    }
}
