package net.http;


import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import net.util.JSONUtils;
import net.util.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;


public class ApiRequest {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequest.class);
    private String uri;
    private Map<String, List<String>> params;
    private Map<String, String> rawParams;
    private HttpHeaders headers;
    private String body;
    private boolean encrypted = false;
    private String ip;
    private String url;
    private String controller;
    private String invokeMethod;

    private ApiRequest(String ip, String _uri, HttpHeaders headers, Map<String, List<String>> params) {
        this.ip = ip;
        this.uri = _uri;
        this.url = "http://" + headers.get(HttpHeaderNames.HOST) + this.uri;
        this.params = params;
        this.headers = headers;
        int index = this.uri.indexOf("?");
        if (index > 0) {
            this.uri = this.uri.substring(0, index);
        }

        index = this.uri.lastIndexOf("/");
        if (0 == index) {
            this.controller = this.uri.substring(1);
            this.invokeMethod = null;
        } else if (index > 0 && index + 1 < this.uri.length()) {
            this.controller = this.uri.substring(1, index);
            this.invokeMethod = this.uri.substring(index + 1);
        }

    }

    public static ApiRequest create(String ip, HttpRequest httpRequest) {
        String uri = httpRequest.getUri();
        Map<String, List<String>> params = new HashMap<>();
        int index = uri.indexOf("?");
        if (0 < index) {
            String content = uri.substring(index + 1);

            try {
                List<NameValuePair> list = URLEncodedUtils.parse(content, Charset.forName("UTF-8"));
                if (null != list) {
                    int i = 0;

                    for(int size = list.size(); i < size; ++i) {
                        params.put((list.get(i)).getName(), new ArrayList(Arrays.asList(new String[]{(list.get(i)).getValue()})));
                    }
                }
            } catch (Exception ex) {
                logger.info("could not pasrse content={}, err={}", content, ex);
            }
        }

        return new ApiRequest(ip, uri, httpRequest.headers(), params);
    }

    public String getHeader(String name) {
        return null != this.headers ? this.headers.get(name) : null;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getControllerUri() {
        return this.controller;
    }

    public String getInvokeMethod() {
        return this.invokeMethod;
    }

    public String uri() {
        return this.uri;
    }

    public String ip() {
        return this.ip;
    }

    public String url() {
        return this.url;
    }

    public Integer getParamAsInt(String key) {
        return NumberUtils.parse(this.getParamAsStr(key));
    }

    public Integer getParamAsInt(String key, Integer defaultVal) {
        return NumberUtils.parse(this.getParamAsStr(key), defaultVal);
    }

    public Map<String, String> getRawData() {
        return this.rawParams;
    }

    public String getParamAsStr(String key) {
        List<String> vals = (List)this.params.get(key);
        return null == vals ? null : (0 == vals.size() ? "" : (String)vals.get(0));
    }


    public String getParamAsStr(String key, String defaultVal) {
        String val = this.getParamAsStr(key);
        return null == val ? defaultVal : val;
    }

    public Integer getRawParamAsInt(String key) {
        String val = this.getRawParamAsStr(key);
        return null == val ? null : NumberUtils.parse(val);
    }

    public BigDecimal getRawParamAsBigDecimal(String key) {
        String val = this.getParamAsStr(key);
        return NumberUtils.toBigDecimal(val);
    }

    public String getRawParamAsStr(String key) {
        Object val = this.rawParams.get(key);
        return null == val ? null : val.toString();
    }

    public <T> T getRawParamAsObject(String key, Class<T> clazz) {
        String val = this.getRawParamAsStr(key);
        return null == val ? null : JSONUtils.parse(val, clazz);
    }

    public <T> List<T> getRawParamAsObjectArray(String key, Class<T> clazz) {
        Object val = this.rawParams.get(key);
        return null != val && val instanceof List ? JSONUtils.parseArray(JSONUtils.toJSONString(val), clazz) : null;
    }

    public Map<String, String> getParamMap() {
        Map<String, String> data = new HashMap<>();
        if (null != this.params) {
            this.params.forEach((k, v) -> {
                String val = this.getParamAsStr(k);
                if (null == val) {
                    val = "";
                }

                data.put(k, val);
            });
        }

        return data;
    }

    public Map<String, List<String>> getParamMapList() {
        return this.params;
    }

    public String getBody() {
        return this.body;
    }

    public boolean bodyEncrypted() {
        return this.encrypted;
    }

    protected void setBody(String body, AbstractContentAdapter ctnParser) {
        this.body = null == ctnParser ? body : ctnParser.parse(body);
        if (!StringUtils.isEmpty(this.body)) {
            this.encrypted = !body.equals(this.body);

            try {
                this.rawParams = (Map)JSONUtils.parse(this.body, Map.class);
            } catch (Exception var4) {
                ;
            }
        }

        if (null == this.rawParams) {
            this.rawParams = new HashMap<>();
        }

    }
}
