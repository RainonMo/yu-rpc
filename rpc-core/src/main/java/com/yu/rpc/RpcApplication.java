package com.yu.rpc;

import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.config.RpcConfig;
import com.yu.rpc.constant.RpcConstant;
import com.yu.rpc.registry.Registry;
import com.yu.rpc.registry.RegistryFactory;
import com.yu.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig){
        rpcConfig = newRpcConfig;
        log.info("rpc init,config={}",newRpcConfig.toString());
        //注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init,config={}",registryConfig);
        //创建并注册Shutdown Hook,JVM退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));

    }

    public static void init(){
        RpcConfig newRpcConfig;
        try {
            //全局配置文件加载
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);

    }

    public static RpcConfig getRpcConfig(){
        if(rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig==null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
