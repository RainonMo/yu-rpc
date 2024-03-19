package com.yu.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yu.rpc.RpcApplication;
import com.yu.rpc.config.RpcConfig;
import com.yu.rpc.constant.RpcConstant;
import com.yu.rpc.model.RpcRequest;
import com.yu.rpc.model.RpcResponse;
import com.yu.rpc.model.ServiceMetaInfo;
import com.yu.rpc.protocol.*;
import com.yu.rpc.registry.Registry;
import com.yu.rpc.registry.RegistryFactory;
import com.yu.rpc.serializer.JdkSerializer;
import com.yu.rpc.serializer.Serializer;
import com.yu.rpc.serializer.SerializerFactory;
import com.yu.rpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        Serializer serializer = null;
//        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
//        for (Serializer service : serviceLoader) {
//            serializer = service;
//        }
        //指定序列化器
        String serviceName = method.getDeclaringClass().getName();
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //构造请求
        RpcRequest rpcRequest = RpcRequest.builder().serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            //序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            //从注册中心获取服务提供者的请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);//名称
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey()); // UserServiceImpl:1.0
            if (CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException("暂无服务地址");
            }
            ServiceMetaInfo selectServiceMetaInfo = serviceMetaInfoList.get(0);

            //发送tcp请求
//            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, serviceMetaInfo);
//            return rpcResponse.getData();
            //发送tcp请求
//            Vertx vertx = Vertx.vertx();
//            NetClient netClient = vertx.createNetClient();
//            CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
//            netClient.connect(selectServiceMetaInfo.getServicePort(),selectServiceMetaInfo.getServiceHost(),result->{
//                if (result.succeeded()){
//                    System.out.println("Connect to TCP server");
//                    NetSocket socket = result.result();
//                    //发送数据
//                    //构造消息
//                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
//                    ProtocolMessage.Header header = new ProtocolMessage.Header();
//                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
//                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
//                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
//                    header.setRequestId(IdUtil.getSnowflakeNextId());
//                    protocolMessage.setHeader(header);
//                    protocolMessage.setBody(rpcRequest);
//                    //编码请求
//                    Buffer encodeBuffer = null;
//                    try {
//                        encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
//                        socket.write(encodeBuffer);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    //接收响应
//                    socket.handler(buffer -> {
//                        try {
//                            //解码响应
//                            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
//                            responseFuture.complete(rpcResponseProtocolMessage.getBody());
//                        } catch (IOException e) {
//                            throw new RuntimeException("协议消息解码错误");
//                        }
//
//                    });
//
//                }else {
//                    System.out.println("Failed to connect to TCP server");
//                }
//            });
//            RpcResponse rpcResponse = responseFuture.get();
//            netClient.close();
//            return rpcResponse.getData();

            try (

                    //发送请求
//                HttpResponse httpResponse = HttpRequest.post("http://"+RpcApplication.getRpcConfig().getServerHost()+":"+RpcApplication.getRpcConfig().getServerPort())
                HttpResponse httpResponse = HttpRequest.post(selectServiceMetaInfo.getServiceAddress())
                        .body(bodyBytes)
                        .execute()) {
                byte[] result = httpResponse.bodyBytes();
                //反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
