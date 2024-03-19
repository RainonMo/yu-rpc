package com.yu.rpc.registry;

import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.model.ServiceMetaInfo;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class RegistryTest  {
    final Registry registry = new EtcdRegistry();

    @Before
    public void init(){
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://81.71.157.57:2380");
        registry.init(registryConfig);
    }

    @Test
    public void register() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceAddress("localhost:123");
        registry.register(serviceMetaInfo);
//        serviceMetaInfo = new ServiceMetaInfo();
//        serviceMetaInfo.setServiceName("myService");
//        serviceMetaInfo.setServiceVersion("1.0");
//        serviceMetaInfo.setServiceHost("localhost");
//        serviceMetaInfo.setServicePort(1235);
//        registry.register(serviceMetaInfo);
//        serviceMetaInfo = new ServiceMetaInfo();
//        serviceMetaInfo.setServiceName("myService");
//        serviceMetaInfo.setServiceVersion("2.0");
//        serviceMetaInfo.setServiceHost("localhost");
//        serviceMetaInfo.setServicePort(1234);
//        registry.register(serviceMetaInfo);
    }
}