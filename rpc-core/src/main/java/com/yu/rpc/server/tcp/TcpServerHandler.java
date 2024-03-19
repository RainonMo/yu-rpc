package com.yu.rpc.server.tcp;

import com.yu.rpc.model.RpcRequest;
import com.yu.rpc.model.RpcResponse;
import com.yu.rpc.protocol.ProtocolMessage;
import com.yu.rpc.protocol.ProtocolMessageDecoder;
import com.yu.rpc.protocol.ProtocolMessageEncoder;
import com.yu.rpc.protocol.ProtocolMessageTypeEnum;
import com.yu.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;


public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            //接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
               throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            //处理请求
            RpcResponse rpcResponse = new RpcResponse();

            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance());

                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setException(e);
                rpcResponse.setMessage(e.getMessage());
            }

            //发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
               throw new RuntimeException("协议消息编码错误");
            }

        });
        netSocket.handler(bufferHandlerWrapper);
//        //处理连接
//        netSocket.handler(buffer -> {
//            //接受请求，解码
//            ProtocolMessage<RpcRequest> protocolMessage;
//            try {
//                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
//            } catch (IOException e) {
//               throw new RuntimeException("协议消息解码错误");
//            }
//            RpcRequest rpcRequest = protocolMessage.getBody();
//
//            //处理请求
//            RpcResponse rpcResponse = new RpcResponse();
//
//            try {
//                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
//                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
//                Object result = method.invoke(implClass.newInstance());
//
//                rpcResponse.setData(result);
//                rpcResponse.setDataType(method.getReturnType());
//                rpcResponse.setMessage("ok");
//            } catch (Exception e) {
//                e.printStackTrace();
//                rpcResponse.setException(e);
//                rpcResponse.setMessage(e.getMessage());
//            }
//
//            //发送响应，编码
//            ProtocolMessage.Header header = protocolMessage.getHeader();
//            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
//            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
//            try {
//                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
//                netSocket.write(encode);
//            } catch (Exception e) {
//               throw new RuntimeException("协议消息编码错误");
//            }
//
//
//        });
    }
}
