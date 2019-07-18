package com.apifan.spider.common.util;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

/**
 * jsoup工具类
 *
 * @author yin
 */
public class JsoupUtils {

    /**
     * 加载HTML文件得到文档对象
     *
     * @param htmlFile HTML文件
     * @param charset  字符集
     * @return 文档对象
     */
    public static Document getDocument(File htmlFile, String charset) {
        try {
            return Jsoup.parse(htmlFile, StringUtils.isEmpty(charset) ? Charsets.UTF_8.name() : charset);
        } catch (IOException e) {
            throw new RuntimeException("无法解析html文件" + htmlFile.getAbsolutePath());
        }
    }

    /**
     * 加载HTML文件得到文档对象
     *
     * @param htmlFile HTML文件
     * @return 文档对象
     */
    public static Document getDocument(File htmlFile) {
        return getDocument(htmlFile, Charsets.UTF_8.name());
    }

    /**
     * 判断是否为空元素
     *
     * @param ele 元素
     * @return
     */
    public static boolean isEmptyElement(Element ele) {
        if (ele == null) {
            return true;
        }
        return !ele.hasText();
    }
}
