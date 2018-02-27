package xyz.fz.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by fz on 2015/8/30.
 */
public class HttpUtil {

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    private static final SSLContext TRUST_ALL_SSL_CONTEXT;

    static {
        try {
            TRUST_ALL_SSL_CONTEXT = SSLContext.getInstance("SSL");
            TRUST_ALL_SSL_CONTEXT.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory TRUST_ALL_SSL_SOCKET_FACTORY = TRUST_ALL_SSL_CONTEXT.getSocketFactory();

    private static OkHttpClient client = new OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(TRUST_ALL_SSL_SOCKET_FACTORY, (X509TrustManager) TRUST_ALL_CERTS[0])
            .hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            })
            .build();

    public static String httpGet(String url, LinkedHashMap queryParams) {
        String queryUrl = queryUrlBuild(url, queryParams);
        Request request = requestBuild(queryUrl);
        logger.debug("Http Get Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static String queryUrlBuild(String url, LinkedHashMap queryParams) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (queryParams != null) {
            urlBuilder.append("?");
            for (Object o : queryParams.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            url = urlBuilder.toString();
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String httpPost(String url, Map formParams) {
        FormBody formBody = formBodyBuild(formParams);
        Request request = requestBuild(url, formBody);
        logger.debug("Http Post Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static FormBody formBodyBuild(Map formParams) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (formParams != null) {
            for (Object o : formParams.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                formBodyBuilder.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return formBodyBuilder.build();
    }

    public static String httpPostJson(String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = requestBuild(url, requestBody);
        logger.debug("Http httpPostJson Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpPostJson(String url, Map<String, String> requestHeaders, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = requestBuild(url, requestHeaders, requestBody);
        logger.debug("Http httpPostJson Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpPostXml(String url, String xml) {
        RequestBody requestBody = RequestBody.create(XML, xml);
        Request request = requestBuild(url, requestBody);
        logger.debug("Http httpPostXml Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpUpload(String url, Map<String, Object> params) {
        MultipartBody multipartBody = multipartBodyBuild(params);
        Request request = requestBuild(url, multipartBody);
        logger.debug("Http Upload Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static MultipartBody multipartBodyBuild(Map<String, Object> params) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry entry : params.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof File) {
                File file = (File) value;
                multipartBodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse("image/jpg"), file));
            } else {
                multipartBodyBuilder.addFormDataPart(key, value.toString());
            }
        }
        return multipartBodyBuilder.build();
    }

    private static Request requestBuild(String url) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        return requestBuilder.build();
    }

    private static Request requestBuild(String url, RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }

    private static Request requestBuild(String url, Map<String, String> requestHeaders, RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        for (Map.Entry entry : requestHeaders.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            requestBuilder.addHeader(key, value);
        }
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }

    private static String responseResult(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    return responseBody.string();
                }
            }
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(httpGet("http://www.baidu.com", null));
    }
}
