package com.yu.rpc.springboot.starter.bootstrap;

import com.yu.rpc.RpcApplication;
import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.config.RpcConfig;
import com.yu.rpc.model.ServiceMetaInfo;
import com.yu.rpc.registry.LocalRegistry;
import com.yu.rpc.registry.Registry;
import com.yu.rpc.registry.RegistryFactory;
import com.yu.rpc.springboot.starter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Rpc服务提供者启动
 * 作用：获取@rpcservice的信息，并通过注解的属性和反射机制，获取到要注册的服务信息，完成服务注册
 * 方案一：主动扫描包
 * 方案二：利用spring的特性监听bean的加载
 * 实现BeanPostProessor接口的postProcessAfterInitialization方法
 */
public class RpcProviderBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService!=null) {
            //获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String servicVersion = rpcService.servicVersion();
            //注册服务
            LocalRegistry.register(serviceName, beanClass);

            //注册服务到注册中心
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

            //构造注册元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(servicVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
