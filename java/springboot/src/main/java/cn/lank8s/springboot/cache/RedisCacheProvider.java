package cn.lank8s.springboot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
@Slf4j
public class RedisCacheProvider implements CacheProvider{

    public final ScheduledExecutorService storageThreadPool;
    
    final StringRedisTemplate stringRedisTemplate;
    
    public String byteToString(byte[] key){
        return new String(key);
    }

    public RedisCacheProvider(StringRedisTemplate stringRedisTemplate, ScheduledExecutorService storageThreadPool) throws IOException {
        if(storageThreadPool==null) {
            this.storageThreadPool = Executors.newScheduledThreadPool(1);
        }else{
            this.storageThreadPool=storageThreadPool;
        }
        this.stringRedisTemplate=stringRedisTemplate;
    }

    @Override
    public void put(byte[] key, byte[] value) throws IOException {
        putAsync(key,value).join();
    }

    @Override
    public CompletableFuture<Void> putAsync(byte[] key, byte[] value){
        CompletableFuture cf = new CompletableFuture();
        storageThreadPool.execute(()->{
            try {
                stringRedisTemplate.opsForValue().set(byteToString(key),byteToString(value));
                cf.complete(null);
            }catch (Exception e){
                log.error("put failed!",e);
                cf.completeExceptionally(e);
            }
        });
        return cf;
    }

    @Override
    public void delete(byte[] key) throws IOException {
    }

    @Override
    public byte[] get(byte[] key) throws IOException {
        try {
            String cache = stringRedisTemplate.opsForValue().get(byteToString(key));
            if(!StringUtils.isEmpty(cache)){
                return cache.getBytes();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sync() throws IOException {
    }

    @Override
    public Set<String> keys() {
//        RocksIterator rocksIterator = db.newIterator();
        return Collections.emptySet();
    }

    @Override
    public void close() throws IOException {
    }
}
