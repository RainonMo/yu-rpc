package com.yu.rpc.registry;

import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {
    void init(RegistryConfig registryConfig);

    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    void unRegister(ServiceMetaInfo serviceMetaInfo);

    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    void destroy();

    void heartBeat();

    /**
     * 监听（消费端）
     */
    void watch(String serviceNodeKey);
}
