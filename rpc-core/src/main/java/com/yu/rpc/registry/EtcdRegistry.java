package com.yu.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.google.common.primitives.Bytes;
import com.yu.rpc.config.RegistryConfig;
import com.yu.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.vertx.core.impl.ConcurrentHashSet;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {
    private Client client;
    private KV kvClient;
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    //注册中心服务缓存
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    //正在监听的key集合
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
       client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
       kvClient = client.getKVClient();
       heartBeat();

    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //创建lease和kv客户端
        Lease leaseClient = client.getLeaseClient();
        //创建一个30秒的租约
        long leaseId = leaseClient.grant(30).get().getID();
        //设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey();// /rpc/UserServiceImpl:1.0/地址
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        //将键值对与租约管理起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key,value,putOption).get();

        //添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH+ serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey(),StandardCharsets.UTF_8));
        //从本地缓存移除
        localRegisterNodeKeySet.remove(registerKey);

    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        //优先从缓存获取服务
        List<ServiceMetaInfo> cacheServiceMetaInfoList = registryServiceCache.readCache();
        if (cacheServiceMetaInfoList!=null){
            return cacheServiceMetaInfoList;
        }
        String searchPrefix = ETCD_ROOT_PATH+serviceKey+"/"; // /rpc/UserServiceImpl:1.0/

        try {
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                    getOption
            ).get().getKvs();
//            return keyValues.stream().map(KeyValue->{
//                String value = KeyValue.getValue().toString(StandardCharsets.UTF_8);
//                return JSONUtil.toBean(value,ServiceMetaInfo.class);
//            }).collect(Collectors.toList());
            //解析服务信息 todo
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream().map(KeyValue -> {
                String key = KeyValue.getKey().toString(StandardCharsets.UTF_8);
                watch(key);
                String value = KeyValue.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
            //写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败",e);
        }
    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key+"节点下线失败");
            }
        }
        if (kvClient!=null){
            kvClient.close();
        }
        if (client!=null){
            client.close();
        }

    }

    @Override
    public void heartBeat() {
        //10秒续签一次
        CronUtil.schedule("*/10 * * * * *",new Task(){

            @Override
            public void execute() {
                //遍历本节点所有的key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        //该节点已过期
                        if (CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        //节点未过期，重新注册
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                       throw new RuntimeException(key+"续签失败",e);
                    }
                }
            }
        });
        //支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();

    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        //之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch){
            watchClient.watch(ByteSequence.from(serviceNodeKey,StandardCharsets.UTF_8),response ->{
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()){
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }

    }
}
