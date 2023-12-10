package cn.lank8s.springboot.service;

import cn.lank8s.springboot.cache.RedisCacheProvider;
import cn.lank8s.springboot.dto.LocationOption;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Data
public class CacheService {
    
    @Autowired
    RedisCacheProvider redisCacheProvider;

    public String getKey(LocationOption opt){
        return "lank8s:location:"+opt.getHost()+":"+opt.getRepo()+":"+opt.getTag();
    }

    public byte[] getKeyForByte(LocationOption opt){
        System.out.println(getKey(opt));
        return getKey(opt).getBytes();
    }
    
    public String getLocation(LocationOption opt){
        byte[] key = getKeyForByte(opt);
        try {
//            byte[] cache = rocksdbCacheProvider.get(key);
            byte[] cache = redisCacheProvider.get(key);
            if (cache != null) {
                return new String(cache);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    
    public void cacheLocation(LocationOption opt, String location){
        byte[] key = getKeyForByte(opt);
        byte[] cache= location.getBytes();
//        rocksdbCacheProvider.putAsync(key,cache);
        redisCacheProvider.putAsync(key,cache);
    }
    
}
