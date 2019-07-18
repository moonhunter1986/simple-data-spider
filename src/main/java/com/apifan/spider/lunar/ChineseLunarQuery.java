package com.apifan.spider.lunar;

import com.apifan.spider.common.util.HttpUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 中国农历日期查询
 * 从紫金山天文台网站进行权威农历数据的查询
 *
 * @author yin
 */
public class ChineseLunarQuery {
    private static final Logger logger = LoggerFactory.getLogger(ChineseLunarQuery.class);

    /**
     * 查询接口地址
     */
    private static final String BASE_URL = "http://almanac.pmo.ac.cn/cgi-bin/cx/gnlcx.pl?saveas=0&year=%d&month=%d&day=%d";

    /**
     * 临时路径
     */
    private String tmpPath = System.getProperty("user.home");

    /**
     * 构造函数
     *
     * @param path 临时文件的存储路径
     */
    public ChineseLunarQuery(String path) {
        if (StringUtils.isNotEmpty(path)) {
            this.tmpPath = path;
        }
    }

    /**
     * 处理
     *
     * @param year  公历年
     * @param month 公历月
     * @param day   公历日
     * @return 包含农历信息的数组，依次为：年,月,日
     */
    public String[] process(int year, int month, int day) {
        File file = query(year, month, day);
        if (file == null) {
            throw new RuntimeException("查询天文台数据失败");
        }
        try {
            String[] array = parseLunar(file);
            if (!file.delete()) {
                logger.error("无法删除临时文件: {}", file.getAbsolutePath());
            }
            return array;
        } catch (IOException e) {
            logger.error("解析农历数据失败", e);
        }
        return null;
    }

    /**
     * 解析农历
     *
     * @param htmlFile 数据文件
     * @return 包含农历信息的数组，依次为：年,月,日
     * @throws IOException
     */
    private String[] parseLunar(File htmlFile) throws IOException {
        if (htmlFile == null || !htmlFile.exists()) {
            throw new RuntimeException("文件不存在");
        }
        Document doc = Jsoup.parse(htmlFile, "GBK");
        Elements elements = doc.select("td[align=left]");
        if (elements == null || elements.size() < 2) {
            throw new RuntimeException("解析异常");
        }
        Element yearElement = elements.get(0);
        String year = yearElement.text().trim().replace("农历", "");
        Element dayElement = elements.get(1);
        String monthAndDay = dayElement.text().trim().replaceAll("\uE003", "");
        int index = monthAndDay.indexOf("月") + 1;
        String month = monthAndDay.substring(0, index);
        String day = monthAndDay.substring(index);
        return new String[]{year, month, day};
    }

    /**
     * 查询
     *
     * @param year  公历年
     * @param month 公历月
     * @param day   公历日
     * @return 数据文件
     */
    private File query(int year, int month, int day) {
        String url = String.format(BASE_URL, year, month, day);
        try {
            File file = new File(this.tmpPath + File.separator + year + "-" + month + "-" + day + ".html");
            if (!file.exists()) {
                HttpUtils.download(url, file.getAbsolutePath());
            }
            return file;
        } catch (IOException e) {
            logger.error("文件下载失败", e);
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        //测试
        String COLUMN_SEPARATOR = "\t";
        List<String> outLines = Lists.newArrayList();

        ChineseLunarQuery query = new ChineseLunarQuery("E:\\tmp\\calendar");

        LocalDate beginDateInclusive = LocalDate.of(2019, 6, 1);
        LocalDate endDateExclusive = LocalDate.of(2019, 6, 11);
        LocalDate date = beginDateInclusive;
        while(date.isBefore(endDateExclusive)){
            String[] tmp = query.process(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            logger.info("日期 {} 对应的结果: {}", date, Arrays.toString(tmp));

            String line = date.toString() + COLUMN_SEPARATOR + tmp[0] + COLUMN_SEPARATOR + tmp[1] + COLUMN_SEPARATOR + tmp[2];
            outLines.add(line);

            date = date.plusDays(1);
            Thread.sleep(1000L);
        }
        File outFile = new File(System.getProperty("user.home") + File.separator + "lunar.txt");
        FileUtils.writeLines(outFile, Charsets.UTF_8.name(), outLines, System.getProperty("line.separator"));
    }

}
