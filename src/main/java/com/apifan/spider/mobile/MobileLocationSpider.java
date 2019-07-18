package com.apifan.spider.mobile;

import com.apifan.spider.common.util.HttpUtils;
import com.apifan.spider.common.util.JsoupUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
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
import java.util.*;

/**
 * 手机号段归属地数据更新
 *
 * @author yin
 */
public class MobileLocationSpider {
    private static final Logger logger = LoggerFactory.getLogger(MobileLocationSpider.class);

    /**
     * TAB
     */
    private static final String TAB = "\t";

    /**
     * 中国移动号段
     */
    private static final List<String> CMCC_LIST = Lists.newArrayList(
            "1340", "1341", "1342", "1343", "1344", "1345", "1346", "1347", "1348",
            "135", "136", "137", "138", "139", "1440", "147", "148", "150", "151", "152", "157", "158", "159",
            "172", "178", "182", "183", "184", "187", "188", "198"
    );

    /**
     * 中国联通号段
     */
    private static final List<String> CUC_LIST = Lists.newArrayList(
            "130", "131", "132", "145", "146", "155", "156", "166", "175", "176", "185", "186"
    );

    /**
     * 中国电信号段
     */
    private static final List<String> CTC_LIST = Lists.newArrayList(
            "133", "1349", "1410", "149", "153", "173", "1740", "177", "180", "181", "189", "191", "199"
    );

    /**
     * 虚拟运营商号段
     */
    private static final List<String> VIRTUAL_LIST = Lists.newArrayList("170", "171", "165", "167");

    /**
     * 卫星通讯号段
     */
    private static final List<String> SATCOM_LIST = Lists.newArrayList("1749");

    /**
     * 基础URL
     */
    private static final String baseURL = "http://www.bixinshui.com/";

    /**
     * 日期标签
     */
    private String dateTag;

    /**
     * 输出路径
     */
    private String outPath;

    /**
     * 索引文件
     */
    private Document indexDocument;

    /**
     * 映射
     */
    private Map<String, String> cityDetailUrlMap = new LinkedHashMap<>();

    /**
     * 构造函数
     *
     * @param outPath 输出路径
     */
    public MobileLocationSpider(String outPath) {
        super();
        this.dateTag = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        this.outPath = outPath;
        File outDir = new File(this.outPath);
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                throw new RuntimeException("无法创建输出目录");
            }
        }
    }

    /**
     * 解析号段数据
     *
     * @return
     * @throws Exception
     */
    public List<MobileLocation> parseMobileLocation() throws Exception {
        if (this.cityDetailUrlMap == null || this.cityDetailUrlMap.isEmpty()) {
            return null;
        }
        List<MobileLocation> mobileLocationList = Lists.newArrayList();

        //下载地市页面
        for (Map.Entry<String, String> entry : this.cityDetailUrlMap.entrySet()) {
            String detailFilePath = outPath + File.separator + entry.getKey() + ".html";
            File detailFile = new File(detailFilePath);
            if (!detailFile.exists()) {
                //暂停1秒
                Thread.sleep(1000L);
                HttpUtils.download(entry.getValue(), detailFilePath);
                logger.info("已下载 {} 的号段数据文件", entry.getValue());
            }

            //解析地市名称（含省份）
            Document cityDocument = JsoupUtils.getDocument(detailFile);
            String cityName = cityDocument.title();
            if (StringUtils.isEmpty(cityName)) {
                logger.error("解析到的地市名称为空");
                continue;
            }
            cityName = cityName.replace(" 手机号码段查询", "").replaceAll(" ", "");

            //解析号段
            String mobileSeletor = "table.table > tbody > tr > td > a";
            Elements mobileElements = cityDocument.select(mobileSeletor);
            int mobilePrefixCount = mobileElements.size();
            logger.info("地市 {} 包含 {} 个号段", cityName, mobilePrefixCount);

            String[] provinceCity = cityName.split("\\-");
            for (Element mobilePrefixElement : mobileElements) {
                if (mobilePrefixElement == null || !mobilePrefixElement.hasText()) {
                    continue;
                }
                String mobilePrefix = mobilePrefixElement.text().trim();
                Optional<String> carrierName = Optional.ofNullable(findCarrierName(mobilePrefix));

                MobileLocation location = new MobileLocation();
                location.setMobilePrefix(mobilePrefix);
                location.setProvince(provinceCity[0]);
                location.setCity(provinceCity[1]);
                location.setCarrierName(carrierName.orElse("未知"));
                mobileLocationList.add(location);
            }
        }
        return mobileLocationList;
    }

    /**
     * 输出到文件
     *
     * @param mobileLocationList 号段数据
     * @return
     * @throws IOException
     */
    public String writeToFile(List<MobileLocation> mobileLocationList) throws IOException {
        if (CollectionUtils.isEmpty(mobileLocationList)) {
            return null;
        }
        List<String> mobilePrefixList = Lists.newArrayList();
        for (MobileLocation location : mobileLocationList) {
            String line = location.getProvince() + TAB + location.getCity() + TAB + location.getMobilePrefix() + TAB + location.getCarrierName();
            mobilePrefixList.add(line);
        }
        File outFile = new File(this.outPath + File.separator + "result_" + dateTag + ".txt");
        FileUtils.writeLines(outFile, Charsets.UTF_8.name(), mobilePrefixList, System.getProperty("line.separator"));
        return outFile.getAbsolutePath();
    }

    /**
     * 处理
     *
     * @return
     */
    public List<MobileLocation> process() {
        try {
            this.downloadIndexFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.parseCityUrl();
        try {
            return this.parseMobileLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载索引文件
     *
     * @return
     * @throws IOException
     */
    private void downloadIndexFile() throws IOException {
        String cityIndexPageUrl = baseURL + "/index.html";
        //索引页面
        String cityIndexFilePath = outPath + File.separator + "index.html";
        File cityIndexFile = new File(cityIndexFilePath);
        if (!cityIndexFile.exists()) {
            HttpUtils.download(cityIndexPageUrl, cityIndexFilePath);
            logger.info("索引文件 {} 已下载", cityIndexFilePath);
        } else {
            logger.info("索引文件 {} 已存在", cityIndexFilePath);
        }
        this.indexDocument = JsoupUtils.getDocument(cityIndexFile);
    }

    /**
     * 解析地市URL
     *
     * @return
     */
    private void parseCityUrl() {
        String selector = "table.table > tbody > tr > td > a";
        Elements elements = this.indexDocument.select(selector);
        if (elements == null || elements.isEmpty()) {
            throw new RuntimeException("无法解析到地市URL");
        }
        //建立地市名称与相应URL的映射
        for (Element element : elements) {
            if (element == null || !element.hasText()) {
                continue;
            }
            String cityText = element.text().trim();
            if (cityText.startsWith("1")) {
                continue;
            }
            this.cityDetailUrlMap.put(element.text().trim(), baseURL + element.attr("href").trim());
        }
        logger.info("总共找到 {} 个地市的号段数据", this.cityDetailUrlMap.size());
    }

    /**
     * 匹配运营商
     *
     * @param mobilePrefix 手机号码前7位
     * @return 运营商名称
     */
    private String findCarrierName(String mobilePrefix) {
        if (StringUtils.isEmpty(mobilePrefix) || mobilePrefix.length() != 7) {
            return null;
        }
        if (matchMobilePrefix(SATCOM_LIST, mobilePrefix)) {
            return "卫星通讯";
        }
        if (matchMobilePrefix(CMCC_LIST, mobilePrefix)) {
            return "中国移动";
        }
        if (matchMobilePrefix(CUC_LIST, mobilePrefix)) {
            return "中国联通";
        }
        if (matchMobilePrefix(CTC_LIST, mobilePrefix)) {
            return "中国电信";
        }
        if (matchMobilePrefix(VIRTUAL_LIST, mobilePrefix)) {
            return "虚拟运营商";
        }
        return null;
    }

    /**
     * 匹配号码前缀
     *
     * @param prefixList   前缀列表
     * @param mobilePrefix 待匹配的号码前缀
     * @return 是否匹配
     */
    private boolean matchMobilePrefix(List<String> prefixList, String mobilePrefix) {
        if (CollectionUtils.isEmpty(prefixList) || StringUtils.isEmpty(mobilePrefix)) {
            return false;
        }
        for (String prefix : prefixList) {
            if (mobilePrefix.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) throws Exception {
        MobileLocationSpider updater = new MobileLocationSpider("E:\\Data\\MobileLocation");
        List<MobileLocation> locationList = updater.process();
        updater.writeToFile(locationList);
    }
}
