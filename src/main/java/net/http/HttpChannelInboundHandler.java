package net.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import net.util.GzipUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class HttpChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger("accessLog");
    private AbstractControllerAdapter controllerAdapter;
    private AbstractContentAdapter ctnAdapter;
    private ApiRequest request;
    private String ip;
    private ByteBuffer buf = null;

    public HttpChannelInboundHandler(AbstractControllerAdapter adapter) {
        controllerAdapter = adapter;
    }

    public HttpChannelInboundHandler(AbstractControllerAdapter adapter, AbstractContentAdapter ctnAdapter) {
        this.controllerAdapter = adapter;
        this.ctnAdapter = ctnAdapter;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long start = System.currentTimeMillis();
        try {
            ApiResponse response = null;
            if (msg instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) msg;
                if (httpRequest.getMethod().name().equalsIgnoreCase("POST")) {
                    int contentLen = Integer.parseInt(httpRequest.headers().get("Content-Length"));
                    buf = ByteBuffer.allocate(contentLen);
                }
                ip = getRemoteIP(ctx, httpRequest);

                try {
                    request = ApiRequest.create(ip, httpRequest);
                } catch (Exception ex) {
                    logger.error("create request", ex);
                    response = ApiResponse.httpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
            } else if (msg instanceof LastHttpContent) {
                copy2Buf((HttpContent) msg);

                try {
                    ApiController controller = controllerAdapter.getControllerByUri(request.getControllerUri());
                    if (null == controller) {
                        logger.warn("invalid uri={}, remoteAddr={}", request.uri(), ip);
                        response = ApiResponse.httpStatus(HttpResponseStatus.NOT_FOUND);
                    } else {
                        request.setBody(getPostData(), ctnAdapter);
                        response = controller.invoke(request);
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                    response = ApiResponse.httpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }

                DefaultFullHttpResponse httpResponse = response.wrapHttpResponse(ctnAdapter, request.bodyEncrypted());
                ctx.write(httpResponse);
                logger.info("request url={} from {} - response_code={}, cost {}ms",
                        new Object[]{request.url(), ip, httpResponse.status().code(), System.currentTimeMillis() - start});
            } else if (msg instanceof HttpContent) {
                copy2Buf((HttpContent) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        super.channelReadComplete(ctx);

        try {
            ctx.channel().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            if (null != ctx) {
                logger.warn("This Exception fired by remoteAddr={}", ctx.channel().remoteAddress().toString());
                ctx.channel().close();
            }
        } catch (Exception e) {
            ;
        }
    }

    private void copy2Buf(HttpContent msg) {
        if (null != buf) {
            ByteBuf content = msg.content();
            int len = content.readableBytes();
            if (len > 0) {
                byte[] bytes = new byte[len];
                content.getBytes(content.readerIndex(), bytes);
                buf.put(bytes);
            }
        }
    }

    private String getPostData() {
        int offset = 0;
        int length = null == buf ? -1 : buf.position();
        String content = null;
        if (0 < length) {
            try {
                buf.flip();
                byte[] dst = new byte[length];
                buf.get(dst, offset, length);
                String contentEncoding = request.getHeader("content-encoding");
                if (null != contentEncoding && "gzip".equalsIgnoreCase(contentEncoding)) {
                    dst = GzipUtils.decompress(dst);
                }
                content = new String(dst);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                buf.clear();
            }
        }

        return content;
    }

    /**
     * 获取用户ip
     * 默认优先socket ip
     * 如果socket ip 为内网地址，从http header里获取用户ip
     *
     * @param ctx
     * @param request
     * @return
     */
    public static String getRemoteIP(ChannelHandlerContext ctx, HttpRequest request) {
        HttpHeaders heads = request.headers();
        // elb ip
        String ip = heads.get("x-forwarded-for");

        // socket ip
        String socketIp = null;
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            socketIp = socketAddress.getAddress().getHostAddress();
            if (!StringUtils.isEmpty(socketIp) && isValidIp(socketIp)) {
                ip = socketIp;
            }
        }
        if (StringUtils.isEmpty(ip)) {
            ip = heads.get("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = heads.get("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = heads.get("WL-Proxy-Client-IP");
        }

        if (!StringUtils.isEmpty(ip)) {
            ip = ip.trim();
            if (ip.indexOf(",") > 0) {
                String[] arr = ip.trim().split(",");
                ip = arr[0];
                for (String str : arr) {
                    if (isValidIp(str)) {
                        ip = str.trim();
                        break;
                    }
                }
            }
        }

        if (StringUtils.isEmpty(ip)) {
            ip = socketIp;
        }
        return ip;
    }

    /**
     * 检测ip是否有效
     * 暂时只支持ip4
     * 本地局域网ip
     * 127.*
     * 10.0.0.0~10.255.255.255
     * 172.16.0.0~172.31.255.255
     * 192.168.0.0~192.168.255.255
     * 保留地址：http://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xhtml
     */
    public static boolean isValidIp(String ip) {

        try {
            if (ip != null && !ip.isEmpty()) {
                String[] strArr = ip.trim().split("\\.");
                if (strArr.length == 4) {
                    int a = Integer.parseInt(strArr[0]);
                    int b = Integer.parseInt(strArr[1]);
                    if (a == 10 || a == 127) {
                        return false;
                    }
                    if (a == 192 && b == 168) {
                        return false;
                    }
                    if (a == 172 && b >= 16 && b <= 31) {
                        return false;
                    }
                    if (a == 169 && b == 254) {
                        return false;
                    }

                    if (a == 100 && b >= 64 && b <= 127) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }
}
