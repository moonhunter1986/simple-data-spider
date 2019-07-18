package com.apifan.spider.football;

import com.apifan.spider.common.util.HtmlUtils;
import com.apifan.spider.common.util.HttpUtils;
import com.apifan.spider.common.util.JsonUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 足球赛程信息爬虫
 *
 * @author yin
 */
public class FootballMatchSpider {
    private static final Logger logger = LoggerFactory.getLogger(FootballMatchSpider.class);

    /**
     * 基础URL
     */
    private static final String BASE_URL = "https://www.zhibo8.cc/";

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 当天日期
     */
    private String date;

    /**
     * 索引文件
     */
    private File indexFile;

    /**
     * 构造函数
     *
     * @param basePath 基础路径
     */
    public FootballMatchSpider(String basePath) {
        super();
        this.basePath = StringUtils.isNotEmpty(basePath) ? basePath.trim() : System.getProperty("user.home");
        this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 下载索引文件
     *
     * @throws IOException
     */
    private void downloadIndex() throws IOException {
        this.basePath = this.basePath + File.separator + ".football_match" + File.separator + date;
        File dir = new File(this.basePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("无法创建目录" + this.basePath);
            }
        }
        this.indexFile = new File(this.basePath + File.separator + "index.html");
        if (!this.indexFile.exists()) {
            HttpUtils.download(BASE_URL, this.indexFile.getAbsolutePath());
            logger.info("已下载索引文件 {}", this.indexFile.getAbsolutePath());
        }
    }

    /**
     * 解析足球赛事信息列表
     *
     * @return 足球赛事信息列表
     */
    private List<FootballMatch> parseFootballMatchList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<FootballMatch> matchList = Lists.newArrayList();

        Document doc = JsoupUtils.getDocument(this.indexFile);
        Elements uls = doc.select("div.content > ul");
        if (CollectionUtils.isEmpty(uls)) {
            throw new RuntimeException("解析到的赛事信息为空");
        }
        for (Element ul : uls) {
            Elements lis = ul.select("li");
            for (Element li : lis) {
                if (li == null) {
                    continue;
                }
                String label = li.attr("label");
                if (StringUtils.isEmpty(label) || !label.contains("足球")) {
                    continue;
                }
                String[] tags = label.split(",");
                if (tags.length < 3 || StringUtils.isEmpty(tags[0]) || "null".equalsIgnoreCase(tags[0])) {
                    continue;
                }
                String id = li.attr("id");
                if (StringUtils.isEmpty(id)) {
                    continue;
                }

                FootballMatch match = new FootballMatch();
                match.setId(id.replace("saishi", ""));

                //赛事名称
                match.setMatchName(tags[0]);
                //主场球队名称
                match.setHomeTeam(tags[1]);
                //客场球队名称
                match.setAwayTeam(tags[2]);
                //开始时间
                String beginTime = li.attr("data-time");
                if (StringUtils.isNotEmpty(beginTime)) {
                    match.setBeginTime(LocalDateTime.parse(beginTime, formatter));
                }

                //解析赛事附加信息
                String innerHtml = li.html();
                String[] tmp = innerHtml.split(" ");
                if (tmp.length > 1) {
                    match.setMatchExtraInfo(HtmlUtils.getSafeText(tmp[1]));
                }
                matchList.add(match);
                logger.info("{}", JsonUtils.toJson(match));
            }
        }
        return matchList;
    }

    /**
     * 处理
     *
     * @return 足球赛事信息列表
     */
    public List<FootballMatch> process() {
        try {
            this.downloadIndex();
        } catch (IOException e) {
            logger.error("下载索引文件失败", e);
            return null;
        }
        return this.parseFootballMatchList();
    }

    public static void main(String[] args) {
        FootballMatchSpider spider = new FootballMatchSpider(System.getProperty("user.home"));
        List<FootballMatch> list = spider.process();
    }
}
