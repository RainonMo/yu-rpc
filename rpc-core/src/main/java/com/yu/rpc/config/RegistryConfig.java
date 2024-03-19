package com.yu.rpc.config;

import com.yu.rpc.registry.RegistryKeys;
import lombok.Data;

@Data
public class RegistryConfig {
    //注册中心类别
    private String registry = RegistryKeys.ZOOKEEPER;
    //注册中心地址
    private String address = "localhost:2181";
    //用户名
    private String username;
    //密码
    private String password;
    //连接超时时间
    private Long timeout = 10000L;

}
