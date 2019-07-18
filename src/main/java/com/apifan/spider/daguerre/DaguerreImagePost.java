package com.apifan.spider.daguerre;

import java.io.Serializable;
import java.util.List;

/**
 * 达盖尔图片帖子信息
 *
 * @author yin
 */
public class DaguerreImagePost implements Serializable {
    private static final long serialVersionUID = 1309970840146179552L;

    /**
     * 帖子ID
     */
    private String postId;

    /**
     * 帖子标题(将会被用作目录名)
     */
    private String postTitle;

    /**
     * 图片URL列表
     */
    private List<String> imageUrlList;

    /**
     * 获取 帖子标题(将会被用作目录名)
     *
     * @return postTitle 帖子标题(将会被用作目录名)
     */
    public String getPostTitle() {
        return this.postTitle;
    }

    /**
     * 设置 帖子标题(将会被用作目录名)
     *
     * @param postTitle 帖子标题(将会被用作目录名)
     */
    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    /**
     * 获取 图片URL列表
     *
     * @return imageUrlList 图片URL列表
     */
    public List<String> getImageUrlList() {
        return this.imageUrlList;
    }

    /**
     * 设置 图片URL列表
     *
     * @param imageUrlList 图片URL列表
     */
    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    /**
     * 获取 帖子ID
     *
     * @return postId 帖子ID
     */
    public String getPostId() {
        return this.postId;
    }

    /**
     * 设置 帖子ID
     *
     * @param postId 帖子ID
     */
    public void setPostId(String postId) {
        this.postId = postId;
    }
}
