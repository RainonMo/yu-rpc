package com.yu.rpc.springboot.starter.annotation;

import com.yu.rpc.springboot.starter.bootstrap.RpcConsumerBootstrap;
import com.yu.rpc.springboot.starter.bootstrap.RpcInitBootstrap;
import com.yu.rpc.springboot.starter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动rpc注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {
    boolean needServer() default true;

}
