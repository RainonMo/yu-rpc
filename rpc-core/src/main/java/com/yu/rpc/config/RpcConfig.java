package com.yu.rpc.config;

import com.yu.rpc.serializer.SerializerKeys;
import lombok.Data;

@Data
public class RpcConfig {
    private String name="yurpc";
    private String version="1.0";
    private String serverHost="localhost";
    private Integer serverPort=8080;
    private boolean mock=false;
    private String serializer= SerializerKeys.JDK;
    private RegistryConfig registryConfig=new RegistryConfig();


}
