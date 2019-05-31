package net.main;

import net.http.AbstractControllerAdapter;
import net.http.ApiController;
import net.server.HttpServer;
import net.util.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NetServer {

    public static void main(String[] args) {
        int port = 8082;
        if (null != args && 0 < args.length && null != NumberUtils.parse(args[0])) {
            port = NumberUtils.parse(args[0], 80);
        }
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:application-context.xml");
        HttpServer server = new HttpServer(new AbstractControllerAdapter() {
            public ApiController getControllerByUri(String uri) {
                ApiController controller = null;
                if (!StringUtils.isEmpty(uri) && ctx.containsBean(uri)) {
                    controller = ctx.getBean(uri, ApiController.class);
                }
                return controller;
            }
        });
        server.startup(port, Boolean.TRUE.booleanValue());
    }
}