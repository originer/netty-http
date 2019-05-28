package net.http;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import net.util.GzipUtils;
import net.util.JSONUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private HttpResponseStatus status;
    private AsciiString contentType;
    private Map<String, String> headProps;
    private String responseBody;
    private Map<String, Object> data;

    static String localIp;

    static {
//        try {
//            localIp = InetAddress.getLocalHost().getHostAddress();
//        } catch (Exception e) {
//        }
    }

    private ApiResponse(HttpResponseStatus status) {
        this.status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        this.contentType = HttpHeaderValues.TEXT_PLAIN;
        this.status = status;
    }

    public DefaultFullHttpResponse wrapHttpResponse() {
        return this.wrapHttpResponse(HttpHeaderValues.KEEP_ALIVE, (AbstractContentAdapter) null, false);
    }

    public DefaultFullHttpResponse wrapHttpResponse(AbstractContentAdapter adapter, boolean requestCtnEncrypted) {
        return this.wrapHttpResponse(HttpHeaderValues.KEEP_ALIVE, adapter, requestCtnEncrypted);
    }

    public DefaultFullHttpResponse wrapHttpResponse(AsciiString keepAlive, AbstractContentAdapter adapter, boolean encrypt) {
        String content = null == this.responseBody ? (null == this.data ? null : JSONUtils.toJSONString(this.data)) : this.responseBody;
        byte[] bytes = StringUtils.isEmpty(content) ? new byte[0] : (null != adapter && encrypt ? adapter.wrap(content) : content).getBytes();
        if (null != this.headProps && this.headProps.containsKey(HttpHeaderNames.CONTENT_ENCODING.toString())) {
            String type = (String) this.headProps.get(HttpHeaderNames.CONTENT_ENCODING.toString());
            if (HttpHeaderValues.GZIP.toString().equalsIgnoreCase(type)) {
                bytes = GzipUtils.compress(bytes);
            }
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, this.status, Unpooled.wrappedBuffer(bytes));
        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, this.contentType + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        heads.add(HttpHeaderNames.CONNECTION, keepAlive);
//        heads.add("x-hm-transid", UUID.randomUUID().toString());
//        heads.add("x-hm-ts", String.valueOf(System.currentTimeMillis()));
//        heads.add("x-hm-ep", localIp);
        if (null != this.headProps) {
            this.headProps.forEach((k, v) -> {
                heads.add(k, v);
            });
        }

        return response;
    }

    public ApiResponse setResponseMsg(Object msg) {
        if (!(msg instanceof String) && !(msg instanceof Number)) {
            this.contentType = HttpHeaderValues.APPLICATION_JSON;
            this.responseBody = JSONUtils.toJSONString(msg);
        } else {
            this.responseBody = msg.toString();
        }

        return this;
    }

    public ApiResponse responseHtml(String msg) {
        this.contentType = new AsciiString("text/html");
        this.responseBody = null == msg ? "" : msg;
        return this;
    }

    public ApiResponse add(String key, Object value) {
        if (null == key) {
            throw new IllegalArgumentException("reponse data key could not be null !");
        } else {
            if (null == this.data) {
                this.contentType = new AsciiString(HttpHeaderValues.APPLICATION_JSON);
                this.data = new HashMap<>();
            }

            if (null != value) {
                this.data.put(key, value);
            }

            return this;
        }
    }

    public static ApiResponse ok() {
        return httpStatus(HttpResponseStatus.OK);
    }

    public static ApiResponse httpStatus() {
        return httpStatus(HttpResponseStatus.BAD_REQUEST);
    }

    public static ApiResponse httpStatus(HttpResponseStatus status) {
        return new ApiResponse(status);
    }

    public ApiResponse addHead(String key, String value) {
        if (null == this.headProps) {
            this.headProps = new HashMap<>();
        }

        this.headProps.put(key, value);
        return this;
    }

    public ApiResponse addHead(AsciiString key, String value) {
        return this.addHead(key.toString(), value);
    }

    public ApiResponse addHead(AsciiString key, AsciiString value) {
        return this.addHead(key.toString(), value.toString());
    }
}
