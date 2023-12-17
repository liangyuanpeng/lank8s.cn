package cn.lank8s.springboot.region;

import cn.lank8s.springboot.dto.Config;

import java.util.concurrent.CompletableFuture;

public class EtcdConfigWatcher implements ConfigWatcher{
    @Override
    public CompletableFuture<Void> update(Config config) {
        return null;
    }
}
