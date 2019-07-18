package com.apifan.spider.area;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 从中华人民共和国民政部网站抓取县以上行政区划代码数据（精确到区/县一级）
 * 地址：http://www.mca.gov.cn/article/sj/xzqh/2019/
 *
 * @author yin
 */
public class AreaDataUpdater {
    private static final Logger logger = LoggerFactory.getLogger(AreaDataUpdater.class);

    /**
     * 列分隔符
     */
    private static final String COLUMN_SEPARATOR = "\t";

    /**
     * 地区编码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 数据文件下载地址
     */
    private String fileUrl;

    /**
     * CSS选择器
     */
    private String cssSelector;

    /**
     * 输出路径
     */
    private String outPath;

    /**
     * 解析出来的元素节点
     */
    private Elements elements;

    /**
     * 行政区划编码集合
     */
    private Set<String> areaCodeSet = new HashSet<>();

    /**
     * 构造函数(输出路径为当前系统用户的主目录)
     *
     * @param fileUrl     数据文件下载地址
     * @param cssSelector CSS选择器
     */
    public AreaDataUpdater(String fileUrl, String cssSelector) {
        this(fileUrl, cssSelector, null);
    }

    /**
     * 构造函数
     *
     * @param fileUrl     数据文件下载地址
     * @param cssSelector CSS选择器
     * @param outPath     输出路径
     */
    public AreaDataUpdater(String fileUrl, String cssSelector, String outPath) {
        super();
        Optional<String> url = Optional.ofNullable(fileUrl);
        this.fileUrl = url.orElse("http://www.mca.gov.cn/article/sj/xzqh/2019/201901-06/201906211048.html");

        Optional<String> selector = Optional.ofNullable(cssSelector);
        this.cssSelector = selector.orElse("td[class=xl6520925]");

        Optional<String> path = Optional.ofNullable(outPath);
        this.outPath = path.orElse(System.getProperty("user.home"));
    }

    /**
     * 处理
     *
     * @return
     */
    public String process() {
        Elements elements = parseElements();
        if (elements == null) {
            throw new RuntimeException("无法解析到行政区划信息");
        }
        this.elements = elements;
        return processAreaNodes();
    }

    /**
     * 解析
     *
     * @return
     */
    private Elements parseElements() {
        //临时数据文件
        String tmpFilePath = this.outPath + File.separator + "tmp_" + System.currentTimeMillis() + ".html";
        try {
            HttpUtils.download(this.fileUrl, tmpFilePath);
            logger.info("已下载临时文件 {}", tmpFilePath);
        } catch (IOException e) {
            logger.error("下载数据文件出错", e);
            return null;
        }
        File tmpFile = new File(tmpFilePath);
        Document document;
        try {
            document = Jsoup.parse(tmpFile, Charsets.UTF_8.name());
        } catch (IOException e) {
            logger.error("解析原始数据失败", e);
            return null;
        }
        Elements elements = document.select(cssSelector);
        if (!tmpFile.delete()) {
            logger.error("无法删除临时文件 {}", tmpFilePath);
        }
        return elements;
    }

    /**
     * 处理行政区划数据节点
     */
    private String processAreaNodes() {
        if (this.elements == null || this.elements.isEmpty()) {
            throw new RuntimeException("解析到的原始数据为空");
        }
        //元素个数
        int elementsCount = this.elements.size();

        //行政区划信息列表
        List<AreaNode> areaNodeList = Lists.newArrayList();

        int i = 0;
        while (i < elementsCount) {
            //编码节点
            Element codeElement = this.elements.get(i);
            i++;

            //名称节点
            Element nameElement = this.elements.get(i);
            i++;

            if (isEmptyElement(codeElement) || isEmptyElement(nameElement)) {
                continue;
            }

            AreaNode area = new AreaNode();
            area.setCode(codeElement.text().trim());
            area.setName(nameElement.text().trim());
            areaNodeList.add(area);
            this.areaCodeSet.add(area.getCode());
        }
        if (areaNodeList.isEmpty()) {
            throw new RuntimeException("解析到的行政区划数据为空");
        }
        logger.info("累计已解析到 {} 条行政区划数据", areaNodeList.size());

        //按照编码重新排序
        Collections.sort(areaNodeList, (o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (StringUtils.isEmpty(o1.getCode()) || StringUtils.isEmpty(o2.getCode())) {
                return 0;
            }
            return o1.getCode().compareTo(o2.getCode());
        });

        List<String> outLines = Lists.newArrayListWithCapacity(areaNodeList.size());
        for (AreaNode area : areaNodeList) {
            //处理上下级关系
            findParent(area);

            //准备输出数据
            outLines.add(area.getCode() + COLUMN_SEPARATOR + area.getName() + COLUMN_SEPARATOR + area.getParentCode());
        }

        String dateTag = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String areaDataFilePath = this.outPath + File.separator + "area_" + dateTag + ".txt";
        try {
            FileUtils.writeLines(new File(areaDataFilePath), Charsets.UTF_8.name(), outLines, System.getProperty("line.separator"));
            logger.info("数据已输出到文件: {}", areaDataFilePath);
            return areaDataFilePath;
        } catch (IOException e) {
            logger.error("输出文件时出错", e);
        }
        return null;
    }

    /**
     * 判断是否为空元素
     *
     * @param ele
     * @return
     */
    private boolean isEmptyElement(Element ele) {
        if (ele == null) {
            return true;
        }
        return !ele.hasText();
    }

    /**
     * 查找上级节点
     *
     * @param node
     */
    private void findParent(AreaNode node) {
        if (node == null || StringUtils.isEmpty(node.getCode()) || node.getCode().length() != CODE_LENGTH || this.areaCodeSet == null || this.areaCodeSet.isEmpty()) {
            return;
        }
        if (node.getCode().endsWith("0000")) {
            //末尾四位编码为0000的节点都是省级节点
            node.setParentCode("0");
            return;
        }

        //依次匹配前4位、前3位、前2位
        for (int i = 4; i >= 2; i--) {
            String prefix = node.getCode().substring(0, i);
            String possibleParent = fillZeroes(prefix, CODE_LENGTH - i);
            if (isParent(node.getCode(), possibleParent)) {
                node.setParentCode(possibleParent);
                return;
            }
        }
        logger.error("找不到 {} 的上级节点", node.getCode());
    }

    /**
     * 判断是否为上级节点
     *
     * @param current        当前待判断的节点编码
     * @param possibleParent 可能的上级节点编码
     * @return
     */
    private boolean isParent(String current, String possibleParent) {
        return current != null && possibleParent != null && this.areaCodeSet.contains(possibleParent) && !current.equals(possibleParent);
    }

    /**
     * 补零
     *
     * @param prefix 前缀
     * @param count  数量
     * @return
     */
    private String fillZeroes(String prefix, int count) {
        StringBuilder zeroes = new StringBuilder(prefix);
        for (int i = 0; i < count; i++) {
            zeroes.append("0");
        }
        return zeroes.toString();
    }
}
