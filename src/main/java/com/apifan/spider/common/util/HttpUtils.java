package com.apifan.spider.common.util;

import com.google.common.collect.Maps;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * HTTP工具类
 *
 * @author yin
 */
public class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    private static final String KEY_USER_AGENT = "User-Agent";

    /**
     * 默认的PC UserAgent
     */
    private static final String USER_AGENT_PC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";

    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_WRITE_TIMEOUT = 10000;

    private static Map<String, OkHttpClient> okHttpClientMap = new ConcurrentHashMap<>();

    private static Map<String, List<Cookie>> cookiesMap = new ConcurrentHashMap<>();

    /**
     * 发起GET请求并获得响应
     *
     * @param url         URL
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse get(String url, HttpProxyConfig proxyConfig) throws IOException {
        return get(url, null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig, false);
    }

    /**
     * 发起GET请求并获得响应
     *
     * @param url URL
     * @return
     * @throws IOException
     */
    public static HttpResponse get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    /**
     * 发起GET请求并获得响应
     *
     * @param url         URL
     * @param headersMap  自定义header信息
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse get(String url, Map<String, String> headersMap, HttpProxyConfig proxyConfig) throws IOException {
        return get(url, headersMap, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig, false);
    }

    /**
     * 发起GET请求并获得响应(使用cookies)
     *
     * @param url         URL
     * @param headersMap  自定义header信息
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse getWithCookies(String url, Map<String, String> headersMap, HttpProxyConfig proxyConfig) throws IOException {
        return get(url, headersMap, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig, true);
    }

    /**
     * 发起GET请求并获得响应
     *
     * @param url        URL
     * @param headersMap 自定义header信息
     * @return
     * @throws IOException
     */
    public static HttpResponse get(String url, Map<String, String> headersMap) throws IOException {
        return get(url, headersMap, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, null, false);
    }

    /**
     * 发起GET请求并获得响应
     *
     * @param url            URL
     * @param headersMap     自定义header信息
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理
     * @param withCookies    是否使用cookies
     * @return
     * @throws IOException
     */
    public static HttpResponse get(String url, Map<String, String> headersMap, int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig, boolean withCookies) throws IOException {
        OkHttpClient client = getOkHttpClient(connectTimeout, readTimeout, writeTimeout, proxyConfig, withCookies);
        Request.Builder builder = prepareRequestBuilder(url, headersMap);
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        return getHttpResponse(response);
    }

    /**
     * 向某个URL提交Form请求
     *
     * @param url         URL
     * @param headersMap  自定义header信息
     * @param paramsMap   请求参数
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postForm(String url, Map<String, String> headersMap, Map<String, String> paramsMap, HttpProxyConfig proxyConfig) throws IOException {
        return postForm(url, headersMap, paramsMap, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig);
    }

    /**
     * 向某个URL提交Form请求
     *
     * @param url        URL
     * @param headersMap 自定义header信息
     * @param paramsMap  请求参数
     * @return
     * @throws IOException
     */
    public static HttpResponse postForm(String url, Map<String, String> headersMap, Map<String, String> paramsMap) throws IOException {
        return postForm(url, headersMap, paramsMap, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, null);
    }

    /**
     * 向某个URL提交Form请求
     *
     * @param url            URL
     * @param headersMap     自定义header信息
     * @param paramsMap      请求参数
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postForm(String url, Map<String, String> headersMap, Map<String, String> paramsMap, int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig) throws IOException {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (paramsMap != null && !paramsMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                if (StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())) {
                    formBodyBuilder.add(entry.getKey(), entry.getValue());
                }
            }
        }
        RequestBody body = formBodyBuilder.build();
        return postBody(url, headersMap, body, connectTimeout, readTimeout, writeTimeout, proxyConfig);
    }

    /**
     * 向某个URL提交JSON字符串
     *
     * @param url         URL
     * @param headersMap  自定义header信息
     * @param json        JSON字符串
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postJson(String url, Map<String, String> headersMap, String json, HttpProxyConfig proxyConfig) throws IOException {
        return postJson(url, headersMap, json, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig);
    }

    /**
     * 向某个URL提交JSON字符串
     *
     * @param url        URL
     * @param headersMap 自定义header信息
     * @param json       JSON字符串
     * @return
     * @throws IOException
     */
    public static HttpResponse postJson(String url, Map<String, String> headersMap, String json) throws IOException {
        return postJson(url, headersMap, json, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, null);
    }

    /**
     * 向某个URL提交JSON字符串
     *
     * @param url            URL
     * @param headersMap     自定义header信息
     * @param json           JSON字符串
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postJson(String url, Map<String, String> headersMap, String json, int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig) throws IOException {
        return postBody(url, headersMap, RequestBody.create(JSON, json), connectTimeout, readTimeout, writeTimeout, proxyConfig);
    }

    /**
     * 向某个URL提交XML字符串
     *
     * @param url         URL
     * @param headersMap  自定义header信息
     * @param xml         XML字符串
     * @param proxyConfig 代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postXml(String url, Map<String, String> headersMap, String xml, HttpProxyConfig proxyConfig) throws IOException {
        return postXml(url, headersMap, xml, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, proxyConfig);
    }

    /**
     * 向某个URL提交XML字符串
     *
     * @param url        URL
     * @param headersMap 自定义header信息
     * @param xml        XML字符串
     * @return
     * @throws IOException
     */
    public static HttpResponse postXml(String url, Map<String, String> headersMap, String xml) throws IOException {
        return postXml(url, headersMap, xml, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, null);
    }

    /**
     * 向某个URL提交XML字符串
     *
     * @param url            URL
     * @param headersMap     自定义header信息
     * @param xml            XML字符串
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @return
     * @throws IOException
     */
    public static HttpResponse postXml(String url, Map<String, String> headersMap, String xml, int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig) throws IOException {
        return postBody(url, headersMap, RequestBody.create(XML, xml), connectTimeout, readTimeout, writeTimeout, proxyConfig);
    }

    /**
     * 下载二进制文件
     *
     * @param url         URL
     * @param destPath    文件保存路径
     * @param proxyConfig 代理配置
     * @throws IOException
     */
    public static boolean download(String url, String destPath, HttpProxyConfig proxyConfig) throws IOException {
        return download(url, destPath, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, -1, proxyConfig);
    }

    /**
     * 下载二进制文件
     *
     * @param url      URL
     * @param destPath 文件保存路径
     * @throws IOException
     */
    public static void download(String url, String destPath) throws IOException {
        download(url, destPath, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, -1, null);
    }

    /**
     * 下载二进制文件
     *
     * @param url      URL
     * @param destPath 文件保存路径
     * @param minSize  文件大小的下限阈值(单位: 字节，-1表示无限制)
     * @throws IOException
     */
    public static void download(String url, String destPath, long minSize) throws IOException {
        download(url, destPath, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, minSize, null);
    }

    /**
     * 下载二进制文件
     *
     * @param url            URL
     * @param destPath       文件保存路径
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param minSize        文件大小的下限阈值(单位: 字节，-1表示无限制)
     * @param proxyConfig    代理配置
     * @throws IOException
     */
    public static boolean download(String url, String destPath, int connectTimeout, int readTimeout, int writeTimeout, long minSize, HttpProxyConfig proxyConfig) throws IOException {
        if (StringUtils.isEmpty(destPath)) {
            throw new IllegalArgumentException("文件保存路径为空");
        }
        OkHttpClient client = getOkHttpClient(connectTimeout, readTimeout, writeTimeout, proxyConfig);
        Map<String, String> headersMap = Maps.newHashMap();
        headersMap.put("Accept-Encoding", "identity");
        Request.Builder builder = prepareRequestBuilder(url, headersMap);
        Request request = builder.build();

        InputStream ins = null;
        OutputStream os = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("从 {} 下载文件失败! 状态码: {}", url, response.code());
                return false;
            }
            ResponseBody body = response.body();
            long contentLength = body != null ? body.contentLength() : 0L;
            if (contentLength == 0L || (minSize > 0L && contentLength < minSize)) {
                logger.warn("文件大小 {} 低于本次下载的下限 {} 字节, 跳过! url={}", contentLength, minSize, url);
                return false;
            }
            ins = body.byteStream();
            os = new FileOutputStream(new File(destPath));
            byte[] buffer = new byte[2 * 1024];
            int len;
            while ((len = ins.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            logger.info("已从 {} 下载文件保存到 {}", url, destPath);
            return true;
        } catch (IOException ioe) {
            logger.error("从 {} 下载文件时出现异常", url, ioe);
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (os != null) {
                os.close();
            }
        }
        return false;
    }

    /**
     * 发起POST请求并获得响应数据
     *
     * @param url            URL
     * @param headersMap     自定义header信息
     * @param body           请求体
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @return
     * @throws IOException
     */
    private static HttpResponse postBody(String url, Map<String, String> headersMap, RequestBody body, int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig) throws IOException {
        OkHttpClient client = getOkHttpClient(connectTimeout, readTimeout, writeTimeout, proxyConfig);
        Request.Builder builder = prepareRequestBuilder(url, headersMap);
        Request request = builder.post(body).build();
        Response response = client.newCall(request).execute();
        return getHttpResponse(response);
    }

    /**
     * 设置请求headers
     *
     * @param builder 请求builder
     * @param headers 自定义header信息
     */
    private static void setRequestHeaders(Request.Builder builder, Map<String, String> headers) {
        if (builder == null || headers == null) {
            return;
        }
        if (!headers.containsKey(KEY_USER_AGENT)) {
            headers.put(KEY_USER_AGENT, USER_AGENT_PC);
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(entry.getValue())) {
                continue;
            }
            builder.header(key.trim(), entry.getValue().trim());
        }
    }

    /**
     * 获取OkHttpClient对象(不使用cookies)
     *
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @return
     */
    private static OkHttpClient getOkHttpClient(int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig) {
        return getOkHttpClient(connectTimeout, readTimeout, writeTimeout, proxyConfig, false);
    }

    /**
     * 获取OkHttpClient对象
     *
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout    读超时时间(毫秒)
     * @param writeTimeout   写超时时间(毫秒)
     * @param proxyConfig    代理配置
     * @param withCookies    是否使用cookies
     * @return
     */
    private static OkHttpClient getOkHttpClient(int connectTimeout, int readTimeout, int writeTimeout, HttpProxyConfig proxyConfig, boolean withCookies) {
        StringBuilder cacheKey = new StringBuilder(Objects.toString(withCookies));
        cacheKey.append(connectTimeout).append("_").append(readTimeout).append("_").append(writeTimeout);
        if (proxyConfig != null) {
            cacheKey.append("_").append(proxyConfig);
        }
        return okHttpClientMap.computeIfAbsent(cacheKey.toString(), k -> {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (connectTimeout > 0) {
                builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            }
            if (readTimeout > 0) {
                builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            }
            if (writeTimeout > 0) {
                builder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
            }
            if (proxyConfig != null) {
                builder.proxy(proxyConfig.getProxy());
                if (proxyConfig.isNeedAuth()) {
                    builder.proxyAuthenticator(proxyConfig.getAuthenticator());
                }
            }
            builder.retryOnConnectionFailure(true);
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(256);
            dispatcher.setMaxRequestsPerHost(32);
            builder.dispatcher(dispatcher);
            if (withCookies) {
                builder.cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        if (url != null && cookies != null) {
                            cookiesMap.put(parseDomain(url.toString()), cookies);
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        if (url == null) {
                            return new ArrayList<>();
                        }
                        Optional<List<Cookie>> cookies = Optional.ofNullable(cookiesMap.get(parseDomain(url.toString())));
                        return cookies.orElse(new ArrayList<>());
                    }
                });
            }
            return builder.build();
        });
    }

    /**
     * 获取响应数据
     *
     * @param response Response
     * @return
     * @throws IOException
     */
    private static HttpResponse getHttpResponse(Response response) throws IOException {
        if (response == null) {
            return null;
        }
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setCode(response.code());
        httpResponse.setMessage(response.body() != null ? response.body().string() : null);
        Headers responseHeaders = response.headers();
        Set<String> headerKeys = responseHeaders.names();
        if (!CollectionUtils.isEmpty(headerKeys)) {
            Map<String, String> headersMap = new HashMap<>();
            for (String headerKey : headerKeys) {
                headersMap.put(headerKey, responseHeaders.get(headerKey));
            }
            httpResponse.setHeaders(headersMap);
        }
        response.close();
        return httpResponse;
    }

    /**
     * 准备请求构建器
     *
     * @param url        URL
     * @param headersMap 自定义header信息
     * @return 请求构建器
     */
    private static Request.Builder prepareRequestBuilder(String url, Map<String, String> headersMap) {
        checkURL(url);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        setRequestHeaders(builder, headersMap);
        return builder;
    }

    /**
     * 检查URL
     *
     * @param url URL
     */
    private static void checkURL(String url) {
        if (StringUtils.isEmpty(url) || !url.startsWith("http")) {
            throw new IllegalArgumentException("URL无效");
        }
        if (url.startsWith("https://")) {
            //暂时禁用SNI，避免 JDK1.7 及之后版本出现 javax.net.ssl.SSLProtocolException: handshake alert:  unrecognized_name 错误
            //http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0
            //System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        }
    }

    /**
     * 从URL解析域名
     *
     * @param url
     * @return
     */
    private static String parseDomain(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String[] tmp = url.split("/");
        if (tmp.length >= 3) {
            return tmp[2];
        }
        return url;
    }
}
