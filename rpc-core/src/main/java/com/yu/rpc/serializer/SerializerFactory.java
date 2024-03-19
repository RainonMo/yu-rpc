package com.yu.rpc.serializer;

import com.yu.rpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {
//    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<String,Serializer>(){
//        {
//            put(SerializerKeys.JDK,new JdkSerializer());
//            put(SerializerKeys.HESSIAN,new HessianSerializer());
//        }
//    };
//
//    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get("jdk");
//
//    public static Serializer getInstance(String key){
//        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
//    }

    static{
        SpiLoader.load(Serializer.class);
    }

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    public static Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class,key);
    }
}
