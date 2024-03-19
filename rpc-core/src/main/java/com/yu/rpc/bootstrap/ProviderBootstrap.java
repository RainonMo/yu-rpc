package com.yu.rpc.bootstrap;

import com.yu.rpc.RpcApplication;
import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.config.RpcConfig;
import com.yu.rpc.model.ServiceMetaInfo;
import com.yu.rpc.model.ServiceRegisterInfo;
import com.yu.rpc.registry.LocalRegistry;
import com.yu.rpc.registry.Registry;
import com.yu.rpc.registry.RegistryFactory;
import com.yu.rpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者初始化
 */
public class ProviderBootstrap {
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        //rpc框架初始化
        RpcApplication.init();
        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            //本地注册
            LocalRegistry.register(serviceName,serviceRegisterInfo.getImplClass());
            //注册服务到服务中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            //注册元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                new RuntimeException(serviceName+"服务注册失败",e);
            }
        }

        //启动服务器
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());

    }

}
