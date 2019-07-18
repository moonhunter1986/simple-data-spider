package com.apifan.spider.proxy;

import com.apifan.spider.common.util.HttpUtils;
import com.apifan.spider.common.util.JsoupUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 免费高匿代理服务器信息爬虫
 *
 * @author yin
 */
public class FreeProxySpider {

    private static final String BASE_URL = "https://www.xicidaili.com/";

    /**
     * 起始页码
     */
    private int beginPageNo;

    /**
     * 结束页码
     */
    private int endPageNo;

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 当天日期
     */
    private String date;

    /**
     * 构造函数(从第1页开始)
     *
     * @param pageCount 结束页码(含)
     * @param basePath  基础输出路径
     */
    public FreeProxySpider(int pageCount, String basePath) {
        this(1, pageCount, basePath);
    }

    /**
     * 构造函数
     *
     * @param beginPageNo 起始页码
     * @param pageCount   结束页码(含)
     * @param basePath    基础输出路径
     */
    public FreeProxySpider(int beginPageNo, int pageCount, String basePath) {
        super();
        if (beginPageNo < 1) {
            beginPageNo = 1;
        }
        this.beginPageNo = beginPageNo;
        if (pageCount < 1) {
            pageCount = 1;
        }
        this.endPageNo = pageCount;
        this.basePath = StringUtils.isNotEmpty(basePath) ? basePath : System.getProperty("user.home");
        this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 解析代理列表
     *
     * @return
     * @throws IOException
     */
    private List<FreeProxyInfo> parseProxyList() throws IOException {
        this.basePath = this.basePath + File.separator + ".free_proxy" + File.separator + date;
        File dir = new File(this.basePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("无法创建目录" + this.basePath);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");

        List<FreeProxyInfo> proxyInfoList = Lists.newArrayList();

        for (int i = beginPageNo; i <= endPageNo; i++) {
            File indexFile = new File(this.basePath + File.separator + i + ".html");
            if (!indexFile.exists()) {
                String url = BASE_URL + "nn/" + i;
                HttpUtils.download(url, indexFile.getAbsolutePath());
            }

            Document doc = JsoupUtils.getDocument(indexFile);
            Elements elements = doc.select("#ip_list > tbody > tr");
            if (CollectionUtils.isEmpty(elements)) {
                continue;
            }
            int rowsCount = elements.size();
            //从第二行开始解析
            for (int j = 1; j < rowsCount; j++) {
                Element element = elements.get(j);
                if (JsoupUtils.isEmptyElement(element)) {
                    continue;
                }
                Elements tds = element.select("td");
                if (CollectionUtils.isEmpty(tds)) {
                    continue;
                }

                //第2个td为IP
                Element ipElement = tds.get(1);
                if (JsoupUtils.isEmptyElement(ipElement)) {
                    continue;
                }
                FreeProxyInfo proxyInfo = new FreeProxyInfo();
                proxyInfo.setIp(ipElement.text().trim());
                //第3个td为端口
                proxyInfo.setPort(Integer.parseInt(tds.get(2).text().trim()));
                //第5个td为匿名类型
                proxyInfo.setIsAnonymous(tds.get(4).text().trim());
                //第6个td为类型
                proxyInfo.setType(tds.get(5).text().trim());

                //第7个td为速度
                if (tds.get(6) != null) {
                    Element speedElement = tds.get(6).selectFirst("div");
                    if (speedElement != null) {
                        String speed = speedElement.attr("title").trim();
                        proxyInfo.setSpeed(new BigDecimal(speed.replace("秒", "")));
                    }
                }

                //第9个td为存活时间
                if (!JsoupUtils.isEmptyElement(tds.get(8))) {
                    int minutes = 0;
                    String surviveTime = tds.get(8).text().trim();
                    if (surviveTime.contains("分钟")) {
                        minutes = Integer.parseInt(surviveTime.replace("分钟", ""));
                    } else if (surviveTime.contains("天")) {
                        int days = Integer.parseInt(surviveTime.replace("天", ""));
                        minutes = days * 1440;
                    } else if (surviveTime.contains("小时")) {
                        int hours = Integer.parseInt(surviveTime.replace("小时", ""));
                        minutes = hours * 60;
                    }
                    proxyInfo.setSurviveMinutes(minutes);
                }

                //第10个td为最近验证时间
                if (!JsoupUtils.isEmptyElement(tds.get(9))) {
                    proxyInfo.setVerifyTime(LocalDateTime.parse(tds.get(9).text().trim(), formatter));
                }
                proxyInfoList.add(proxyInfo);
            }
        }
        return proxyInfoList;
    }

    /**
     * 简单测试
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        FreeProxySpider spider = new FreeProxySpider(1, null);
        spider.parseProxyList();
    }
}
