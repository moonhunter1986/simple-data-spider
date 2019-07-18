package com.apifan.spider;

import com.apifan.spider.area.AreaDataUpdater;
import com.apifan.spider.common.util.FileUtils;
import com.apifan.spider.football.FootballMatch;
import com.apifan.spider.football.FootballMatchSpider;
import com.apifan.spider.lunar.ChineseLunarQuery;
import com.apifan.spider.tieba.TiebaKeywordsSpider;
import com.apifan.spider.weibo.WeiboKeywordsSpider;
import com.google.common.base.Charsets;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * 单元测试用例
 *
 * @author yin
 */
public class MySpiderTest {

    /**
     * 测试足球赛程信息爬虫
     */
    @Test
    public void runFootballMatchSpider(){
        FootballMatchSpider spider = new FootballMatchSpider(System.getProperty("user.home"));
        List<FootballMatch> list = spider.process();
        assertTrue(list != null && list.size() > 0);
    }

    /**
     * 测试抓取行政区划代码数据
     */
    @Test
    public void runAreaDataUpdater(){
        //数据文件下载地址
        String fileUrl = "http://www.mca.gov.cn/article/sj/xzqh/2019/201901-06/201906211048.html";
        //CSS选择器
        //说明：原始数据中，地区编码和地区名称为紧邻的两个<td class=xl6520925>元素
        String cssSelector = "td[class=xl6520925]";
        AreaDataUpdater updater = new AreaDataUpdater(fileUrl, cssSelector, System.getProperty("user.home"));
        String resultFile = updater.process();
        File file = new File(resultFile);
        assertTrue(file.exists() && file.length() > 0L);
    }

    /**
     * 测试农历查询
     */
    @Test
    public void runChineseLunarQuery(){
        ChineseLunarQuery query = new ChineseLunarQuery(System.getProperty("user.home"));
        LocalDate date = LocalDate.of(2019, 6, 1);
        String[] tmp = query.process(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        assertTrue(tmp != null && tmp.length == 3 && "己亥年".equals(tmp[0]) && "四月".equals(tmp[1]) && "廿八".equals(tmp[2]));
    }

    /**
     * 测试微博热搜
     */
    @Test
    public void runWeiboKeywordsSpider(){
        String basePath = "D:\\spider\\weibo";
        WeiboKeywordsSpider spider = new WeiboKeywordsSpider(basePath);
        List<String> keywordList = spider.process();
        if (CollectionUtils.isNotEmpty(keywordList)) {
            String areaDataFilePath = basePath + File.separator + "keywords_" + spider.getDate() + ".txt";
            try {
                FileUtils.writeLines(new File(areaDataFilePath), Charsets.UTF_8.name(), keywordList, System.getProperty("line.separator"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assertTrue(CollectionUtils.isNotEmpty(keywordList));
    }

    /**
     * 测试百度贴吧热搜
     */
    @Test
    public void runTiebaKeywordsSpider(){
        String basePath = "D:\\spider\\tieba";
        TiebaKeywordsSpider spider = new TiebaKeywordsSpider(basePath);
        List<String> keywordList = spider.process();
        if (CollectionUtils.isNotEmpty(keywordList)) {
            String areaDataFilePath = basePath + File.separator + "keywords_" + spider.getDate() + ".txt";
            try {
                FileUtils.writeLines(new File(areaDataFilePath), Charsets.UTF_8.name(), keywordList, System.getProperty("line.separator"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assertTrue(CollectionUtils.isNotEmpty(keywordList));
    }
}
