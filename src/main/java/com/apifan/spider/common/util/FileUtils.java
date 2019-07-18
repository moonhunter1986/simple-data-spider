package com.apifan.spider.common.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件工具类
 *
 * @author yin
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 文件名序号缓存
     */
    private static Map<String, AtomicInteger> fileNameIntegerMap = new ConcurrentHashMap<>();


    /**
     * 生成唯一有序文件名
     * 每个前缀达到65535个文件归零
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包含前缀和后缀的唯一有序文件名
     */
    public static String generateSortedUniqueFileName(String prefix, String suffix) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(prefix), "前缀不能为空");
        AtomicInteger i = fileNameIntegerMap.computeIfAbsent(prefix, k -> new AtomicInteger(0));
        if (i.get() > 65535) {
            i.set(0);
        }
        //前缀+16进制顺序数字+后缀
        return prefix + String.format("%04x", i.getAndIncrement()) + Objects.toString(suffix, "");
    }

    /**
     * 删除空目录
     *
     * @param path 路径
     */
    public static void deleteEmptyDirs(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        File base = new File(path);
        if (!base.exists()) {
            return;
        }
        if (!base.isDirectory()) {
            return;
        }
        File[] children = base.listFiles();
        if (children == null || children.length == 0) {
            try {
                deleteDirectory(base);
                logger.info("已删除空目录 {}", path);
            } catch (IOException e) {
                logger.error("无法删除空目录", e);
            }
            return;
        }
        for (File child : children) {
            if (child == null) {
                continue;
            }
            deleteEmptyDirs(child.getAbsolutePath());
        }
    }
}
