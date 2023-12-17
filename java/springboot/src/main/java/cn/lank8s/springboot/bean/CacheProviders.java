package cn.lank8s.springboot.bean;

import cn.lank8s.springboot.cache.RedisCacheProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

@Configuration
public class CacheProviders {

//    @Bean
//    public RocksdbCacheProvider rocksdbCacheProvider(MeterRegistry meterRegistry){
//        try {
//            return new RocksdbCacheProvider(meterRegistry,null);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Bean
    public RedisCacheProvider redisCacheProvider(StringRedisTemplate stringRedisTemplate){
        try {
            return new RedisCacheProvider(stringRedisTemplate,null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
