package com.yu.rpc.server;

import com.yu.rpc.RpcApplication;
import com.yu.rpc.model.RpcRequest;
import com.yu.rpc.model.RpcResponse;
import com.yu.rpc.registry.LocalRegistry;
import com.yu.rpc.serializer.JdkSerializer;
import com.yu.rpc.serializer.Serializer;
import com.yu.rpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        //反序列化请求为对象，并从请求对象中获取参数
//        final JdkSerializer serializer = new JdkSerializer();
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());


        //异步处理http请求
        httpServerRequest.bodyHandler(
                body -> {
                    byte[] bytes = body.getBytes();
                    RpcRequest rpcRequest = null;
                    try {
                        rpcRequest = serializer.deserialize(bytes,RpcRequest.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    RpcResponse rpcResponse = new RpcResponse();
                    if(rpcRequest==null){
                        rpcResponse.setMessage("null");
                        doResponse(httpServerRequest,rpcResponse,serializer);
                        return;
                    }

                    try {
                        //获取要调用的服务实现类，通过反射调用
                        Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                        Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                        Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                        //封装返回结果
                        rpcResponse.setData(result);
                        rpcResponse.setDataType(method.getReturnType());
                        rpcResponse.setMessage("ok");
                    } catch (Exception e) {
                        e.printStackTrace();
                        rpcResponse.setMessage(e.getMessage());
                        rpcResponse.setException(e);
                    }

                    doResponse(httpServerRequest,rpcResponse,serializer);

                }
        );

    }

    void doResponse(HttpServerRequest httpServerRequest, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = httpServerRequest.response().putHeader("content-type", "application/json");
        try {
            byte[] serialize = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialize));
        } catch (Exception e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
