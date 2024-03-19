package com.yu.rpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        //创建实例
        Vertx vertx = Vertx.vertx();

        //创建http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //监听端口并处理请求
        server.requestHandler(
                new HttpServerHandler()
//                request -> {
//                    //处理http请求
//                    System.out.println("1");
//
//                    //发送http响应
//                    request.response()
//                            .putHeader("1","2")
//                            .end("2");
//                }
        );

        //启动http服务器并监听指定端口
        server.listen(
                port,result -> {
                    if (result.succeeded()){
                        System.out.println("正在运行端口："+port);
                    }else {
                        System.out.println("运行失败："+result.cause());
                    }
                }
        );

    }
}
