package com.yu.rpc.springboot.starter.bootstrap;

import com.yu.rpc.RpcApplication;
import com.yu.rpc.config.RpcConfig;
import com.yu.rpc.server.tcp.VertxTcpServer;
import com.yu.rpc.springboot.starter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Rpc框架启动
 * spring框架初始化时，获取@enablerpc注解的属性，并初始化rpc框架
 * 实现spring的ImportBeanDefinitionRegistrar接口，在registerBeanDefinitions方法中获取项目的注解和注解信息
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取enablerpc注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");
        //rpc框架初始化
        RpcApplication.init();
        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        if (needServer){
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        }else {
            log.info("不启动server");
        }
    }
}
