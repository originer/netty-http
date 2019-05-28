package net.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;

public class ApiController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String localIp;

    public ApiController() {
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
        }
    }

    public ApiResponse invoke(ApiRequest request) {
        String methodName = request.getInvokeMethod();
        if (StringUtils.isEmpty(methodName)) {
            methodName = "defaults";
        }
        try {
            Method method = this.getClass().getMethod(methodName, ApiRequest.class);
            ApiResponse obj = (ApiResponse) method.invoke(this, request);
            return obj;
        } catch (NoSuchMethodException e) {
            this.logger.warn("method={} not found !", methodName);
            return ApiResponse.httpStatus(HttpResponseStatus.NOT_FOUND);
        } catch (Exception ex) {
            this.fireException(ex);
            this.logger.warn("Exception caused : {}", ex.getMessage());
            return ApiResponse.httpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void fireException(Exception e) {
    }
}
