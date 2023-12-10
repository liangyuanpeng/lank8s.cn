package cn.lank8s.springboot.cache;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CacheProvider {
    
    //TODO 需要再封装一层序列化层
    
    void put(byte[] key, byte[] value) throws IOException;

    CompletableFuture<Void> putAsync(byte[] key, byte[] value);

    void delete(byte[] key) throws IOException;

    byte[] get(byte[] key) throws IOException;

    void sync() throws IOException;

    // TODO chouxiang
//    RocksIterator keys();
    Set<String> keys();
    public void close() throws IOException;
}
