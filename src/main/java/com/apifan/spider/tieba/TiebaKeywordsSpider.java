package com.apifan.spider.tieba;

import com.apifan.spider.common.util.FileUtils;
import com.apifan.spider.common.util.HttpProxyConfig;
import com.apifan.spider.common.util.HttpUtils;
import com.apifan.spider.common.util.JsoupUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 百度贴吧热门关键词爬虫
 *
 * @author yin
 */
public class TiebaKeywordsSpider {
    private static final Logger logger = LoggerFactory.getLogger(TiebaKeywordsSpider.class);

    private static final String INDEX_URL = "http://tieba.baidu.com/hottopic/browse/topicList?res_type=1";

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
     * 索引文件
     */
    private File indexFile;

    /**
     * 构造函数(使用代理)
     *
     * @param basePath      基础输出路径
     * @param proxyHost     代理服务器IP或主机名
     * @param proxyPort     代理服务器端口号
     * @param proxyUsername 代理服务器用户名(代理服务器不需验证时留空)
     * @param proxyPassword 代理服务器密码(代理服务器不需验证时留空)
     */
    public TiebaKeywordsSpider(String basePath, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        super();
        this.basePath = (StringUtils.isNotEmpty(basePath) ? basePath : FileUtils.getUserDirectoryPath()) + File.separator + "tieba_keywords";
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
    public TiebaKeywordsSpider(String basePath) {
        this(basePath, null, -1, null, null);
    }

    public String getDate(){
        return this.date;
    }

    /**
     * 处理
     *
     * @return
     */
    public List<String> process() {
        try {
            downloadIndex();
            return parseKeywords();
        } catch (Exception e) {
            throw new RuntimeException("解析关键词失败");
        }
    }

    /**
     * 下载索引文件
     */
    private void downloadIndex() throws Exception {
        String indexFilePath = basePath + File.separator + date + ".html";
        indexFile = new File(indexFilePath);
        if (!indexFile.exists()) {
            HttpUtils.download(INDEX_URL, indexFilePath, httpProxy);
        }
    }

    /**
     * 解析关键词
     *
     * @return
     */
    private List<String> parseKeywords() {
        String listSelector = "div.topic-body > div.main > ul > li > div > div > a";
        Document indexDoc = JsoupUtils.getDocument(indexFile);
        if (indexDoc == null) {
            throw new RuntimeException("无法解析索引文件");
        }
        Elements elements = indexDoc.select(listSelector);
        if (CollectionUtils.isEmpty(elements)) {
            throw new RuntimeException("无法解析到任何关键词");
        }
        List<String> keywordList = Lists.newArrayList();
        for (Element element : elements) {
            if (JsoupUtils.isEmptyElement(element)) {
                continue;
            }
            String keyword = element.text();
            if (StringUtils.isEmpty(keyword)) {
                continue;
            }
            keywordList.add(StringUtils.trim(keyword));
        }
        return keywordList;
    }
}
