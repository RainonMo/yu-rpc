package com.yu.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.yu.rpc.model.RpcRequest;
import com.yu.rpc.model.RpcResponse;
import com.yu.rpc.model.ServiceMetaInfo;
import com.yu.rpc.protocol.ProtocolMessage;
import com.yu.rpc.protocol.ProtocolMessageDecoder;
import com.yu.rpc.protocol.ProtocolMessageEncoder;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VertxTcpClient {
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        //发送tcp请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        netClient.connect(serviceMetaInfo.getServicePort(),serviceMetaInfo.getServiceHost(),result->{
            if(!result.succeeded()){
                System.out.println("Failed to connect to TCP server");
                return;
            }
            NetSocket socker = result.result();
            //1.构造消息
            ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setRequestId(IdUtil.getSnowflakeNextId());
            protocolMessage.setHeader(header);
            protocolMessage.setBody(rpcRequest);
            //2.编码请求
            try {
                Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
                //3.发送数据请求
                socker.write(encode);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //4.接受响应
            TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                try {
                    //5.解码数据
                    ProtocolMessage<RpcResponse> decode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                    responseFuture.complete(decode.getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            socker.handler(bufferHandlerWrapper);
        });
        RpcResponse rpcResponse = responseFuture.get();
        //6.关闭
        netClient.close();
        return rpcResponse;

    }
    public void start(){
        //创建vertx实例
        Vertx vertx = Vertx.vertx();
        vertx.createNetClient().connect(8888,"localhost",result->{
            if (result.succeeded()){
                System.out.println("Connected to TCP server");
                NetSocket socket = result.result();
                //发送数据
                for(int i=0;i<1000;i++){
                    socket.write("hello,server!hello,server!hello,server!hello,server!");
                }

                //响应数据
                socket.handler(buffer -> {
                    System.out.println("Received response from server:"+buffer.toString());

                });
            }else {
                System.out.println("Failed to connect to TCP server");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
