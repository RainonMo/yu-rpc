package com.yu.rpc.server.tcp;

import com.yu.rpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

import java.nio.charset.StandardCharsets;


public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        //创建vertx实例
        Vertx vertx = Vertx.vertx();
        //创建tcp服务器
        NetServer server = vertx.createNetServer();
        //处理请求
        server.connectHandler(socket->{
            String testMessage = "hello,server!hello,server!hello,server!hello,server!";
            int messageLength = testMessage.getBytes().length;
            RecordParser parser = RecordParser.newFixed(messageLength);
            parser.setOutput(new Handler<Buffer>() {
                @Override
                public void handle(Buffer buffer) {
                    String str = new String(buffer.getBytes(0, messageLength));
                    System.out.println(str);
                    if (testMessage.equals(str)){
                        System.out.println("good");
                    }
                }
            });
            socket.handler(parser);
//            socket.handler(buffer -> {
//                String testMessage = "hello,server!hello,server!hello,server!hello,server!";
//                int messageLength = testMessage.getBytes().length;
//                if (buffer.getBytes().length<messageLength){
//                    System.out.println("半包，length="+buffer.getBytes().length);
//                    return;
//                }
//                if (buffer.getBytes().length>messageLength){
//                    System.out.println("粘包，length="+buffer.getBytes().length);
//                    return;
//                }
//                String str = new String(buffer.getBytes(0, messageLength));
//                System.out.println(str);
//                if (testMessage.equals(str)){
//                    System.out.println("good");
//                }
//
////                byte[] requestData = buffer.getBytes();
////                byte[] responseData =  handlerRequest(requestData);
////                socket.write(Buffer.buffer(responseData));
//            });
        });
        server.listen(port,request->{
            if (request.succeeded()){
                System.out.println("TCP server started on port "+port);
            }else {
                System.out.println("Failed to start TCP server:"+request.cause());
            }
        });
    }

    private byte[] handlerRequest(byte[] requestData) {
        return "hello,client".getBytes();
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
