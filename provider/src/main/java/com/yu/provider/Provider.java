package com.yu.provider;

import com.yu.common.service.UserService;
import com.yu.rpc.registry.LocalRegistry;
import com.yu.rpc.server.HttpServer;
import com.yu.rpc.server.VertxHttpServer;

public class Provider {
    public static void main(String[] args) {
        //注册服务
        LocalRegistry.register(UserService.class.getName(),UserServiceImpl.class);

        //启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);

    }
}
