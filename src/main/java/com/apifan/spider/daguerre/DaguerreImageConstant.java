package com.apifan.spider.daguerre;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 达盖尔的旗帜-常量
 *
 * @author yin
 */
public final class DaguerreImageConstant {

    /**
     * 基础URL
     */
    public static final String baseUrl = "http://t66y.com/";

    /**
     * 图片扩展名
     */
    public static final List<String> possibleExts = Lists.newArrayList("bmp", "jpg", "jpeg", "png", "gif");

    /**
     * html后缀
     */
    public static final String htmlSuffix = ".html";

    /**
     * 下划线
     */
    public static final String underscore = "_";

    /**
     * 斜线
     */
    public static final String slash = "/";

    /**
     * 下载图片时的最大线程数
     */
    public static final int MAX_THREADS_COUNT = 64;

    /**
     * 标题中可能存在的需排除的敏感词
     */
    public static final String[] keywordsToSkip = new String[]{"无需注册", "图文教程", "必读", "技术贴", "審核", "域名", "帖子", "教程", "慎入", "重口"};

    /**
     * 标题中可能存在的无用关键词
     */
    public static final String[] keywordsToRemove = new String[]{"草榴社區", "-t66y.com", "-達蓋爾的旗幟"};

    /**
     * 基于chevereto搭建的网站URL前缀
     */
    public static final List<String> cheveretoBasedWebsiteUrls = Lists.newArrayList("https://www.simipic.com/image/", "https://www.kanjiantu.com/image/", "https://www.privacypic.com/image/", "https://www.s6tu.com/image/", "http://pic303.com/");
}
