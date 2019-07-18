package com.apifan.spider.fund;

import com.apifan.spider.common.util.HttpUtils;
import com.apifan.spider.common.util.JsonUtils;
import com.google.common.base.Charsets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 开放式基金数据抓取
 *
 * @author yin
 */
public class OpenEndFundsSpider {
    private static final Logger logger = LoggerFactory.getLogger(OpenEndFundsSpider.class);

    /**
     * TAB
     */
    private static final String TAB = "\t";

    private static final String BASE_URL = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t=1&lx=1&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&feature=|&dt=%d&atfc=&onlySale=1";


    public static void main(String[] args) throws Exception {


        String outPath = "D:\\tmp\\fund";

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String filePath = outPath + File.separator + today + ".json";
        File dataFile = new File(filePath);
        if (!dataFile.exists()) {
            HttpUtils.download(String.format(BASE_URL, System.currentTimeMillis()), filePath);
        }

        String json = FileUtils.readFileToString(dataFile, Charsets.UTF_8);
        //处理
        json = json
                .replace("var db=", "")
                .replace("chars:", "\"chars\":")
                .replace("datas:", "\"datas\":")
                .replace("count:", "\"count\":")
                .replace("record:", "\"record\":")
                .replace("pages:", "\"pages\":")
                .replace("curpage:", "\"curpage\":")
                .replace("indexsy:", "\"indexsy\":")
                .replace("showday:", "\"showday\":")
                .replace(",]", "]");

        Map<String, Object> resultMap = JsonUtils.readAsMap(json);
        List<List<String>> fundsList = (List<List<String>>) resultMap.get("datas");
        int fundsCount = fundsList.size();
        logger.info("条数={}", fundsCount);


        List<OpenEndFund> openEndFundList = new ArrayList<>();

        List<String> daysList = (List<String>) resultMap.get("showday");
        if (CollectionUtils.isEmpty(daysList)) {
            throw new RuntimeException("交易日为空");
        }
        int ocDate = Integer.parseInt(daysList.get(0).replaceAll("\\-", ""));
        for (List<String> fundParams : fundsList) {
            if (StringUtils.isEmpty(fundParams.get(0))) {
                continue;
            }
            OpenEndFund fund = new OpenEndFund();
            //基金代码
            fund.setFundCode(fundParams.get(0));
            //基金名称
            fund.setFundName(fundParams.get(1));
            //拼音
            fund.setPinyin(fundParams.get(2));
            //单位净值
            if (StringUtils.isNotEmpty(fundParams.get(3))) {
                fund.setNetAssetValue(new BigDecimal(fundParams.get(3)));
            }
            //累计净值
            if (StringUtils.isNotEmpty(fundParams.get(4))) {
                fund.setNetAccumValue(new BigDecimal(fundParams.get(4)));
            }
            //日增长值
            if (StringUtils.isNotEmpty(fundParams.get(7))) {
                fund.setIncrement(new BigDecimal(fundParams.get(7)));
            }
            //日增长率
            fund.setIncrementRate(parsePercent(fundParams.get(8)));
            //申购状态
            fund.setPurchaseStatus(fundParams.get(9));
            //赎回状态
            fund.setRedemptionStatus(fundParams.get(10));
            //手续费率
            fund.setRedemptionFeeRate(parsePercent(fundParams.get(17)));
            fund.setOcDate(ocDate);
            openEndFundList.add(fund);
        }

        logger.info("基金数量={}", openEndFundList.size());

        //按照日增长倒排序
        Collections.sort(openEndFundList, (o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (o1.getIncrement() == null || o2.getIncrement() == null) {
                return 0;
            }
            return o2.getIncrement().compareTo(o1.getIncrement());
        });

        //输出前1000条
        int outputCount = openEndFundList.size() > 1000 ? 1000 : openEndFundList.size();
        List<String> outLines = new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            OpenEndFund fund = openEndFundList.get(i);
            String line = fund.getFundCode()
                    + TAB + fund.getFundName()
                    + TAB + fund.getPurchaseStatus()
                    + TAB + fund.getRedemptionStatus()
                    + TAB + fund.getNetAssetValue()
                    + TAB + fund.getNetAccumValue()
                    + TAB + fund.getIncrement()
                    + TAB + fund.getIncrementRate()
                    + TAB + fund.getRedemptionFeeRate()
                    + TAB + fund.getOcDate();
            outLines.add(line);
        }
        File outFile = new File(outPath + File.separator + "fund_" + today + ".txt");
        FileUtils.writeLines(outFile, Charsets.UTF_8.name(), outLines, System.getProperty("line.separator"));
    }

    /**
     * 解析百分比
     *
     * @param percent
     * @return
     */
    private static BigDecimal parsePercent(String percent) {
        if (StringUtils.isEmpty(percent)) {
            return null;
        }
        return new BigDecimal(percent.replace("%", "")).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    }

}
