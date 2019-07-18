package com.apifan.spider.lagou;

import com.apifan.spider.common.util.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 拉勾网职位爬虫
 *
 * @author yin
 */
public class LagouPositionSpider {
    private static final Logger logger = LoggerFactory.getLogger(LagouPositionSpider.class);

    /**
     * 日期格式
     */
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 搜索页URL
     */
    private static String searchUrl = "https://m.lagou.com/search.html";

    /**
     * JSON接口URL
     */
    private static String jsonUrl = "https://m.lagou.com/search.json?city=%s&positionName=%s&pageNo=%d";

    /**
     * 代理
     */
    private HttpProxyConfig httpProxy;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 基础输出路径
     */
    private String basePath;

    /**
     * 构造函数
     *
     * @param outputPath    基础输出路径
     * @param proxyHost     代理服务器IP或主机名
     * @param proxyPort     代理服务器端口号
     * @param proxyUsername 代理服务器用户名(代理服务器不需验证时留空)
     * @param proxyPassword 代理服务器密码(代理服务器不需验证时留空)
     */
    public LagouPositionSpider(String outputPath, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        super();
        Preconditions.checkArgument(StringUtils.isNotEmpty(outputPath), "基础输出路径为空");
        basePath = outputPath;
        File dir = new File(outputPath);
        if (dir.exists() && !dir.isDirectory()) {
            throw new RuntimeException("路径 " + outputPath + " 必须是一个目录");
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("无法初始化路径 " + outputPath);
            }
        }
        if (StringUtils.isNotEmpty(proxyHost) && proxyPort > 0) {
            httpProxy = new HttpProxyConfig(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        date = LocalDate.now();
        logger.info("初始化完成。文件输出路径: {}", basePath);
        if (httpProxy != null) {
            logger.info("使用以下HTTP代理: {}:{}", proxyHost, proxyPort);
        } else {
            logger.info("不使用HTTP代理");
        }
    }

    /**
     * 构造函数(不使用代理)
     *
     * @param outputPath 基础输出路径
     */
    public LagouPositionSpider(String outputPath) {
        this(outputPath, null, -1, null, null);
    }


    /**
     * 获取拉勾网的cookies
     *
     * @throws IOException
     */
    private void getLagouCookies() throws IOException {
        HttpUtils.getWithCookies(searchUrl, buildHeaders(false), httpProxy);
    }

    /**
     * 获取职位信息列表
     *
     * @param city    城市名称
     * @param keyword 职位关键词
     * @param pages   总页码
     * @return
     */
    public List<LagouPositionInfo> getPositionList(String city, String keyword, int pages) {
        Preconditions.checkArgument(pages > 0, "页码错误！至少下载1页");
        Preconditions.checkArgument(StringUtils.isNotEmpty(city), "城市名称为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(keyword), "职位关键词为空");

        //检查临时目录
        File tmpDir = new File(basePath + File.separator + "tmp");
        if (tmpDir.exists() && !tmpDir.isDirectory()) {
            throw new RuntimeException("路径 " + tmpDir.getAbsolutePath() + " 必须是一个目录");
        }
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                throw new RuntimeException("无法初始化路径 " + tmpDir.getAbsolutePath());
            }
        }
        try {
            getLagouCookies();
        } catch (IOException e) {
            logger.error("获取拉勾网cookies时出现异常", e);
            throw new RuntimeException("无法获得拉勾网cookies");
        }
        List<File> jsonFiles = Lists.newArrayList();
        Map<String, String> headersMap = buildHeaders(true);
        for (int i = 1; i <= pages; i++) {
            String jsonFilePath = tmpDir.getAbsolutePath() + File.separator + date.format(dateFormatter) + "#" + city + "#" + keyword + "#" + i + ".json";
            File jsonFile = new File(jsonFilePath);
            if (!jsonFile.exists()) {
                //下载文件
                String requestUrl = String.format(jsonUrl, city, keyword, i);
                try {
                    Thread.sleep(1000L * i);
                    HttpResponse httpResponse = HttpUtils.getWithCookies(requestUrl, headersMap, httpProxy);
                    if (httpResponse == null || httpResponse.getCode() != 200) {
                        logger.error("无法下载URL:{}", requestUrl);
                        continue;
                    }
                    FileUtils.write(jsonFile, httpResponse.getMessage(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    logger.error("下载数据失败", e);
                    continue;
                }
            }
            jsonFiles.add(jsonFile);
        }
        return parsePositionList(jsonFiles);
    }

    /**
     * 解析职位信息
     *
     * @param jsonFiles JSON文件列表
     * @return 职位信息列表
     */
    private List<LagouPositionInfo> parsePositionList(List<File> jsonFiles) {
        if (CollectionUtils.isEmpty(jsonFiles)) {
            return null;
        }
        List<LagouPositionInfo> positionList = Lists.newArrayList();
        for (int i = 0; i < jsonFiles.size(); i++) {
            logger.info("开始解析第{}个文件", i + 1);
            File jsonFile = jsonFiles.get(i);
            if (jsonFile == null || !jsonFile.exists()) {
                continue;
            }
            String json = null;
            try {
                json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("无法读取文件 {}", jsonFile.getAbsolutePath(), e);
            }
            Map<String, Object> jsonMap = JsonUtils.readAsMap(json);
            LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) jsonMap.get("content");
            if (content == null || !content.containsKey("data")) {
                continue;
            }
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) content.get("data");
            if (data == null || !data.containsKey("page")) {
                continue;
            }
            LinkedHashMap<String, Object> page = (LinkedHashMap<String, Object>) data.get("page");
            if (page == null || !page.containsKey("result")) {
                continue;
            }
            ArrayList<LinkedHashMap<String, Object>> resultList = (ArrayList<LinkedHashMap<String, Object>>) page.get("result");
            if (CollectionUtils.isEmpty(resultList)) {
                continue;
            }
            for (LinkedHashMap<String, Object> positionMap : resultList) {
                LagouPositionInfo position = new LagouPositionInfo();
                position.setPositionId(Objects.toString(positionMap.get("positionId")));
                position.setPositionName(Objects.toString(positionMap.get("positionName")));
                position.setCity(Objects.toString(positionMap.get("city")));
                position.setSalaryRange(Objects.toString(positionMap.get("salary")));
                if (StringUtils.isNotEmpty(position.getSalaryRange())) {
                    //解析薪水范围
                    String[] tmp = position.getSalaryRange().split("-");
                    if (tmp.length == 2) {
                        position.setSalaryBegin(NumberUtils.parseMoneyText(tmp[0]));
                        position.setSalaryEnd(NumberUtils.parseMoneyText(tmp[1]));
                    }
                }
                position.setCompanyId(Objects.toString(positionMap.get("companyId")));
                position.setCompanyFullName(Objects.toString(positionMap.get("companyFullName")));

                String createTime = Objects.toString(positionMap.get("createTime"));
                if (StringUtils.isNotEmpty(createTime)) {
                    //解析职位日期
                    if (createTime.contains("昨天")) {
                        LocalDate createDate = date.minusDays(-1);
                        position.setCreateDate(createDate.format(dateFormatter));
                    } else if (createTime.contains("前天")) {
                        LocalDate createDate = date.minusDays(-2);
                        position.setCreateDate(createDate.format(dateFormatter));
                    } else {
                        position.setCreateDate(createTime.replaceAll("-", ""));
                    }
                }
                logger.info("已解析成功一条职位信息");
                positionList.add(position);
            }
        }
        return positionList;
    }

    /**
     * 构建请求头参数
     *
     * @param isAjax 是否模拟ajax请求头
     * @return
     */
    private Map<String, String> buildHeaders(boolean isAjax) {
        Map<String, String> headersMap = Maps.newHashMap();
        headersMap.put("User-Agent", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1");
        headersMap.put("Connection", "keep-alive");
        headersMap.put("Accept-Encoding", "gzip, deflate, br");
        headersMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
        if (isAjax) {
            headersMap.put("Accept", "application/json");
            headersMap.put("Host", "m.lagou.com");
            headersMap.put("Referer", searchUrl);
            headersMap.put("X-Requested-With", "XMLHttpRequest");
        }
        return headersMap;
    }

    /**
     * 测试
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String city = "长沙";
        String keyword = "安卓";
        LagouPositionSpider spider = new LagouPositionSpider("D:\\spider\\lagou");
        List<LagouPositionInfo> positionList = spider.getPositionList(city, keyword, 3);

        FileUtils.write(new File("D:\\spider\\lagou\\" + LocalDate.now().format(LagouPositionSpider.dateFormatter) + "#" + city + "#" + keyword + ".txt"), JsonUtils.toJson(positionList, true), StandardCharsets.UTF_8);
    }


}
