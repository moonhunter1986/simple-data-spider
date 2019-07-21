package com.apifan.spider.daguerre;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.apifan.spider.common.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 达盖尔的旗帜图片爬虫
 *
 * @author yin
 */
public class DaguerreImageSpider {
    private static final Logger logger = LoggerFactory.getLogger(DaguerreImageSpider.class);

    /**
     * 总计数器
     */
    private static AtomicInteger totalCount = new AtomicInteger(0);

    /**
     * 旧HTML文件的判定标准(天)
     */
    private static final long OLD_HTML_DAYS = 30L;

    /**
     * 图片大小下限(单位: 字节)
     */
    private long minSize = 30000;

    /**
     * 异步线程池
     */
    private ExecutorService threadPool;

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 当天日期
     */
    private String date;

    /**
     * 是否使用代理
     */
    private boolean useProxy = false;

    /**
     * 代理
     */
    private HttpProxyConfig httpProxy;

    /**
     * 索引文件
     */
    private File indexFile;

    List<String> cheveretoBasedWebsiteUrls = Lists.newArrayList();

    /**
     * 构造函数(使用代理)
     *
     * @param basePath      基础输出路径
     * @param proxyHost     代理服务器IP或主机名
     * @param proxyPort     代理服务器端口号
     * @param proxyUsername 代理服务器用户名(代理服务器不需验证时留空)
     * @param proxyPassword 代理服务器密码(代理服务器不需验证时留空)
     */
    public DaguerreImageSpider(String basePath, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        super();
        Preconditions.checkArgument(StringUtils.isNotEmpty(basePath), "基础输出路径为空");
        this.basePath = StringUtils.isNotEmpty(basePath) ? basePath : FileUtils.getUserDirectoryPath();
        if (StringUtils.isNotEmpty(proxyHost)) {
            this.useProxy = true;
            this.httpProxy = new HttpProxyConfig(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String cheveretoUrlsFile = this.getClass().getResource("/chevereto_urls.txt").getFile();
        try {
            List<String> urls = FileUtils.readLines(new File(cheveretoUrlsFile), StandardCharsets.UTF_8);
            if(CollectionUtils.isNotEmpty(urls)){
                cheveretoBasedWebsiteUrls.addAll(urls);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("初始化完成。文件输出路径: {}", this.basePath);
        if (this.useProxy) {
            logger.info("使用以下HTTP代理: {}:{}", proxyHost, proxyPort);
        } else {
            logger.info("不使用HTTP代理");
        }
    }

    /**
     * 构造函数(不使用代理)
     *
     * @param basePath 基础输出路径
     */
    public DaguerreImageSpider(String basePath) {
        this(basePath, null, -1, null, null);
    }

    /**
     * 处理
     */
    public void process() throws Exception {
        downloadIndex();
        List<DaguerreImagePost> imagePostList = parseImagePosts();
        if (CollectionUtils.isEmpty(imagePostList)) {
            logger.error("没有解析到任何待下载的图片");
            return;
        }
        int postsCount = imagePostList.size();
        logger.info("总共有 {} 个帖子的图片需要下载!", postsCount);
        threadPool = Executors.newFixedThreadPool(postsCount < DaguerreImageConstant.MAX_THREADS_COUNT ? postsCount : DaguerreImageConstant.MAX_THREADS_COUNT);
        for (int i = 0; i < postsCount; i++) {
            DaguerreImagePost post = imagePostList.get(i);
            if (post == null || CollectionUtils.isEmpty(post.getImageUrlList())) {
                continue;
            }
            String dirName = StringUtils.truncate(StringUtils.isNotEmpty(post.getPostTitle()) ? post.getPostTitle().trim() : String.valueOf(System.currentTimeMillis()), 64);
            File dir = new File(basePath + File.separator + dirName);
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                logger.warn("无法初始化目录: {}", dir.getAbsolutePath(), e);
                continue;
            }
            for (String url : post.getImageUrlList()) {
                downloadImage(url, dir.getAbsolutePath(), post.getPostId());
            }
            logger.info("已提交 {} 个帖子到下载队列，总共 {} 个帖子", i + 1, postsCount);
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        logger.info("已关闭线程池。本次下载工作已结束。累计下载成功 {} 张图片。", totalCount.get());
        FileUtils.deleteEmptyDirs(basePath);
        removeOldHtmlFiles();
    }

    /**
     * 下载索引文件
     */
    private void downloadIndex() {
        basePath = basePath + File.separator;
        File outDir = new File(basePath + File.separator + "html");
        try {
            FileUtils.forceMkdir(outDir);
        } catch (IOException e) {
            logger.error("无法初始化目录 {}", basePath, e);
            throw new RuntimeException("无法初始化目录" + basePath);
        }
        indexFile = new File(outDir.getAbsolutePath() + File.separator + "index_" + date + ".html");
        if (!indexFile.exists()) {
            //如果索引文件不存在，则下载它
            try {
                HttpUtils.download(DaguerreImageConstant.baseUrl + "thread0806.php?fid=16&search=today", indexFile.getAbsolutePath(), getHttpProxy());
            } catch (IOException e) {
                logger.error("无法下载索引文件", e);
                throw new RuntimeException("无法下载索引文件" + indexFile.getAbsolutePath());
            }
        }
    }

    /**
     * 解析图片帖子集合
     *
     * @return 图片下载地址
     */
    private List<DaguerreImagePost> parseImagePosts() {
        //解析详细页链接
        Document indexDocument = JsoupUtils.getDocument(indexFile, "GBK");
        String pageSelector = "#ajaxtable > tbody > tr > td.tal > h3 > a";
        Elements pageLinkElements = indexDocument.select(pageSelector);
        if (CollectionUtils.isEmpty(pageLinkElements)) {
            throw new RuntimeException("未找到详细页的链接");
        }

        List<String> detailPageLinkList = Lists.newArrayList();
        for (Element pageLinkElement : pageLinkElements) {
            if (JsoupUtils.isEmptyElement(pageLinkElement)) {
                continue;
            }
            String link = pageLinkElement.attr("href");
            if (StringUtils.isEmpty(link)) {
                continue;
            }
            if (!link.startsWith(DaguerreImageConstant.baseUrl)) {
                link = DaguerreImageConstant.baseUrl + link;
            }
            detailPageLinkList.add(link.trim());
        }
        int detailPageCount = detailPageLinkList.size();
        if (detailPageCount == 0) {
            throw new RuntimeException("没有解析到任何详细页链接");
        }
        logger.info("解析到 {} 个详细页链接", detailPageCount);

        List<DaguerreImagePost> postList = Lists.newArrayList();
        //依次下载详细页
        for (String linkUrl : detailPageLinkList) {
            String postId = parsePostId(linkUrl);
            File htmlFile = new File(basePath + File.separator + "html" + File.separator + generateDetailPageName(postId));
            if (!htmlFile.exists()) {
                try {
                    boolean downloadResult = HttpUtils.download(linkUrl, htmlFile.getAbsolutePath(), getHttpProxy());
                    if(!downloadResult){
                        logger.warn("下载详细页文件失败: URL={}", linkUrl);
                        continue;
                    }
                } catch (IOException e) {
                    logger.warn("无法下载详细页文件", e);
                    continue;
                }
            }

            //解析详细页
            Document detailPageDocument = JsoupUtils.getDocument(htmlFile, "GBK");
            //帖子标题
            String postTitle = getCleanedTitle(detailPageDocument.title());
            if (needsToSkip(postTitle)) {
                //无关帖子排除掉
                logger.warn("帖子 {} 包含敏感词，跳过", postTitle);
                continue;
            }

            String imageSelector = "input[type=image]";
            Elements imageElements = detailPageDocument.select(imageSelector);
            int imageCount = imageElements != null ? imageElements.size() : 0;
            if (imageCount == 0) {
                logger.warn("详细页 {} 没有图片", htmlFile.getAbsolutePath());
                continue;
            }
            logger.info("详细页 {} 可能有 {} 张图片", htmlFile.getAbsolutePath(), imageCount);

            //解析待下载的图片URL
            Set<String> imageUrls = new LinkedHashSet<>();
            for (Element imageElement : imageElements) {
                String imageUrl = imageElement.attr("data-link");
                if (StringUtils.isEmpty(imageUrl) || !imageUrl.startsWith("http")) {
                    continue;
                }
                if (isCheveretoBased(imageUrl)) {
                    //特殊情况特殊处理
                    String realImageUrl = parseRealImageUrlOfChevereto(imageUrl);
                    if (StringUtils.isNotEmpty(realImageUrl)) {
                        imageUrl = realImageUrl;
                    }
                }
                imageUrl = imageUrl.replace("i/?i=", "");
                imageUrls.add(imageUrl);
            }
            if (CollectionUtils.isEmpty(imageUrls)) {
                logger.warn("帖子 {} 没有符合要求的图片", postTitle);
                continue;
            }
            DaguerreImagePost post = new DaguerreImagePost();
            post.setPostId(postId);
            post.setPostTitle(postTitle);
            post.setImageUrlList(Lists.newArrayList(imageUrls));
            postList.add(post);
        }
        return postList;
    }

    /**
     * 异步下载图片文件
     *
     * @param url    文件URL
     * @param dir    文件保存目录的路径
     * @param postId 所属的帖子ID
     */
    private void downloadImage(String url, String dir, String postId) {
        File targetFile = new File(dir + File.separator + getUniqueFileName(url, postId));
        if (targetFile.exists() && targetFile.length() > minSize) {
            logger.error("URL: {} 对应的图片文件已存在", url);
            return;
        }
        threadPool.execute(() -> {
            logger.debug("将 {} 添加到下载队列", url);
            try {
                Thread.sleep(300L);
                boolean downloadSuccess = HttpUtils.download(url, targetFile.getAbsolutePath(), 30000, 60000, 15000, minSize, getHttpProxy());
                if (downloadSuccess) {
                    logger.info("本次已下载成功 {} 张图片", totalCount.incrementAndGet());
                }
            } catch (Exception e) {
                logger.error("从 {} 下载文件失败", url, e);
            }
        });
    }

    /**
     * 生成唯一文件名
     *
     * @param url    文件URL
     * @param postId 所属的帖子ID
     * @return
     */
    private String getUniqueFileName(String url, String postId) {
        String ext = "jpg";
        String[] tmp = url.split("\\.");
        if (tmp.length > 1) {
            String suffix = tmp[tmp.length - 1];
            if (StringUtils.isNotEmpty(suffix)) {
                suffix = suffix.toLowerCase();
                if (DaguerreImageConstant.possibleExts.contains(suffix)) {
                    ext = suffix;
                }
            }
        }
        return FileUtils.generateSortedUniqueFileName(postId + DaguerreImageConstant.underscore, "." + ext);
    }

    /**
     * 是否包含需排除的关键词
     *
     * @param title
     * @return
     */
    private boolean needsToSkip(String title) {
        if (StringUtils.isEmpty(title)) {
            return false;
        }
        return StringUtils.containsAny(title, DaguerreImageConstant.keywordsToSkip);
    }

    /**
     * 解析真实的图片地址
     * 用于Chevereto所搭建的图床网站
     *
     * @param url 图片详细页地址
     * @return
     */
    private String parseRealImageUrlOfChevereto(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        File imageHtml = new File(basePath + File.separator + "html" + File.separator + DigestUtils.md5Hex(url) + ".html");
        if (!imageHtml.exists()) {
            try {
                HttpUtils.download(url, imageHtml.getAbsolutePath(), 10000, 10000, 10000, -1, getHttpProxy());
            } catch (IOException e) {
                logger.error("无法下载图片页面 {}", url, e);
                return null;
            }
        }
        Document imageDocument = JsoupUtils.getDocument(imageHtml);
        Element imageElement = imageDocument != null ? imageDocument.selectFirst("div.header-content-right > a") : null;
        if (imageElement == null) {
            return null;
        }
        String imageUrl = imageElement.attr("href");
        if (StringUtils.isEmpty(imageUrl) || !imageUrl.startsWith("http")) {
            return null;
        }
        logger.info("已解析到真实的图片下载地址: {}", imageUrl);
        return imageUrl;
    }

    /**
     * 判断是否为基于Chevereto的网站
     *
     * @param url
     * @return
     */
    private boolean isCheveretoBased(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        for (String prefix : cheveretoBasedWebsiteUrls) {
            if (url.toLowerCase().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析帖子ID
     *
     * @param url
     * @return
     */
    private String parsePostId(String url) {
        if (StringUtils.isEmpty(url) || !url.contains(DaguerreImageConstant.slash)) {
            throw new RuntimeException("详细页URL为空或无效");
        }
        String[] tmp = url.split(DaguerreImageConstant.slash);
        //取最后一段
        String postId = tmp[tmp.length - 1];
        if (StringUtils.isEmpty(postId) || !postId.endsWith(DaguerreImageConstant.htmlSuffix)) {
            return String.valueOf(System.currentTimeMillis());
        }
        return postId.replace(DaguerreImageConstant.htmlSuffix, "");
    }

    /**
     * 生成详细页保存时所用的文件名
     *
     * @param postId
     * @return
     */
    private String generateDetailPageName(String postId) {
        if (StringUtils.isEmpty(postId)) {
            return System.currentTimeMillis() + DaguerreImageConstant.htmlSuffix;
        }
        return postId + DaguerreImageConstant.htmlSuffix;
    }

    /**
     * 获取清爽的标题文本
     *
     * @param title
     * @return
     */
    private String getCleanedTitle(String title) {
        String postTitle = HtmlUtils.getSafeText(title);
        if (StringUtils.isEmpty(postTitle)) {
            return String.valueOf(System.currentTimeMillis());
        }
        if (StringUtils.containsAny(title, DaguerreImageConstant.keywordsToRemove)) {
            for (String nonsenseKeyword : DaguerreImageConstant.keywordsToRemove) {
                postTitle = postTitle.replaceAll(nonsenseKeyword, "");
            }
        }
        return postTitle;
    }

    /**
     * 获取代理配置
     *
     * @return
     */
    private HttpProxyConfig getHttpProxy() {
        return useProxy ? httpProxy : null;
    }

    /**
     * 删除旧的网页文件
     */
    private void removeOldHtmlFiles() {
        File base = new File(basePath + File.separator + "html");
        if (!base.exists()) {
            return;
        }
        if (!base.isDirectory()) {
            return;
        }
        File[] htmlFiles = base.listFiles();
        if (htmlFiles == null || htmlFiles.length == 0) {
            return;
        }
        //计算截止时间戳
        long timestamp = System.currentTimeMillis() - 86400 * 1000 * OLD_HTML_DAYS;
        for (File htmlFile : htmlFiles) {
            if (!htmlFile.getName().endsWith(".html")) {
                continue;
            }
            if (htmlFile.lastModified() < timestamp) {
                if (FileUtils.deleteQuietly(htmlFile)) {
                    logger.info("已删除旧文件 {}", htmlFile.getAbsolutePath());
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        String basePath = "D:\\spider\\daguerre";
        DaguerreImageSpider downloader = new DaguerreImageSpider(basePath);
        downloader.process();
    }
}
