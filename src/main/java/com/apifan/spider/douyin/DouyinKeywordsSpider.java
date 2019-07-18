package com.apifan.spider.douyin;

import com.apifan.spider.common.util.*;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 抖音热搜关键词爬虫
 *
 * @author yin
 */
public class DouyinKeywordsSpider {
    private static final Logger logger = LoggerFactory.getLogger(DouyinKeywordsSpider.class);

    private static final String INDEX_URL = "https://www.iesdouyin.com/web/api/v2/hotsearch/billboard/word/";

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 当天日期
     */
    private String date;

    /**
     * 代理
     */
    private HttpProxyConfig httpProxy;

    /**
     * 构造函数(使用代理)
     *
     * @param basePath      基础输出路径
     * @param proxyHost     代理服务器IP或主机名
     * @param proxyPort     代理服务器端口号
     * @param proxyUsername 代理服务器用户名(代理服务器不需验证时留空)
     * @param proxyPassword 代理服务器密码(代理服务器不需验证时留空)
     */
    public DouyinKeywordsSpider(String basePath, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        super();
        this.basePath = (StringUtils.isNotEmpty(basePath) ? basePath : FileUtils.getUserDirectoryPath()) + File.separator + "douyin_keywords";
        if (StringUtils.isNotEmpty(proxyHost)) {
            httpProxy = new HttpProxyConfig(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        try {
            FileUtils.forceMkdir(new File(this.basePath));
            logger.info("初始化完成。文件输出路径: {}", this.basePath);
            if (httpProxy != null) {
                logger.info("使用以下HTTP代理: {}:{}", proxyHost, proxyPort);
            } else {
                logger.info("不使用HTTP代理");
            }
        } catch (IOException e) {
            logger.error("无法初始化目录 {}", this.basePath, e);
            throw new RuntimeException("无法初始化目录" + this.basePath);
        }
    }

    /**
     * 构造函数(不使用代理)
     *
     * @param basePath 基础输出路径
     */
    public DouyinKeywordsSpider(String basePath) {
        this(basePath, null, -1, null, null);
    }

    /**
     * 处理
     *
     * @return
     */
    public List<String> process() {
        try {
            return parseKeywords();
        } catch (Exception e) {
            logger.error("解析关键词失败", e);
            throw new RuntimeException("解析关键词失败");
        }
    }

    /**
     * 解析关键词
     *
     * @return
     */
    private List<String> parseKeywords() throws IOException {
        HttpResponse response = HttpUtils.get(INDEX_URL, httpProxy);
        if (response == null || response.getCode() != 200) {
            throw new RuntimeException("请求抖音接口失败");
        }
        String json = response.getMessage();
        Map<String, Object> resultMap = JsonUtils.readAsMap(json);
        if (resultMap == null || resultMap.isEmpty()) {
            logger.error("解析数据失败: {}", json);
            throw new RuntimeException("解析数据失败");
        }
        List<Map<String, Object>> wordsList = (List<Map<String, Object>>) resultMap.get("word_list");
        if (CollectionUtils.isEmpty(wordsList)) {
            throw new RuntimeException("无法解析到任何关键词");
        }

        List<String> keywordList = Lists.newArrayList();
        for (Map<String, Object> wordMap : wordsList) {
            String keyword = Objects.toString(wordMap.get("word"), "");
            if (StringUtils.isEmpty(keyword)) {
                continue;
            }
            keywordList.add(StringUtils.trim(keyword));
        }
        return keywordList;
    }

    /**
     * 测试
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String basePath = "D:\\spider\\douyin";
        DouyinKeywordsSpider spider = new DouyinKeywordsSpider(basePath);
        List<String> keywordList = spider.process();
        if (CollectionUtils.isNotEmpty(keywordList)) {
            String areaDataFilePath = spider.basePath + File.separator + "keywords_" + spider.date + ".txt";
            FileUtils.writeLines(new File(areaDataFilePath), Charsets.UTF_8.name(), keywordList, System.getProperty("line.separator"));
        }
    }
}
