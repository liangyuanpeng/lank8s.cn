package cn.lank8s.springboot.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RocksdbCacheProvider implements CacheProvider{

    private final RocksDB db;

    private final WriteOptions optionSync;
    private final WriteOptions optionDontSync;

    private final ReadOptions optionCache;
    private final ReadOptions optionDontCache;
    private final WriteBatch emptyBatch;

    public final ScheduledExecutorService storageThreadPool;

    public void writeMetrics(MeterRegistry meterRegistry,Statistics statistics){
        meterRegistry.gauge("rocksdb_backup_read_bytes",statistics.getTickerCount(TickerType.BACKUP_READ_BYTES));
        meterRegistry.gauge("rocksdb_backup_write_bytes",statistics.getTickerCount(TickerType.BACKUP_WRITE_BYTES));
        meterRegistry.gauge("rocksdb_blob_db_blob_index_evicted_count",statistics.getTickerCount(TickerType.BLOB_DB_BLOB_INDEX_EVICTED_COUNT));
        meterRegistry.gauge("rocksdb_blob_db_blob_index_expired_count",statistics.getTickerCount(TickerType.BLOB_DB_BLOB_INDEX_EXPIRED_COUNT));
        meterRegistry.gauge("rocksdb_block_cache_compressed_hit",statistics.getTickerCount(TickerType.BLOCK_CACHE_COMPRESSED_HIT));
        meterRegistry.gauge("rocksdb_block_cache_compressed_miss",statistics.getTickerCount(TickerType.BLOCK_CACHE_COMPRESSED_MISS));
        meterRegistry.gauge("rocksdb_blob_db_blob_file_synced",statistics.getTickerCount(TickerType.BLOB_DB_BLOB_FILE_SYNCED));

        meterRegistry.gauge("rocksdb_block_cache_data_bytes_insert",statistics.getTickerCount(TickerType.BLOCK_CACHE_DATA_BYTES_INSERT));
        meterRegistry.gauge("rocksdb_block_cache_data_add",statistics.getTickerCount(TickerType.BLOCK_CACHE_DATA_ADD));
        meterRegistry.gauge("rocksdb_block_cache_data_hit",statistics.getTickerCount(TickerType.BLOCK_CACHE_DATA_HIT));
        meterRegistry.gauge("rocksdb_block_cache_miss",statistics.getTickerCount(TickerType.BLOCK_CACHE_DATA_MISS));

        meterRegistry.gauge("rocksdb_bytes_read",statistics.getTickerCount(TickerType.BYTES_READ));
        meterRegistry.gauge("rocksdb_bytes_written",statistics.getTickerCount(TickerType.BYTES_WRITTEN));
        meterRegistry.gauge("rocksdb_write_timeout",statistics.getTickerCount(TickerType.WRITE_TIMEDOUT));
        meterRegistry.gauge("rocksdb_write_with_wal",statistics.getTickerCount(TickerType.WRITE_WITH_WAL));

        meterRegistry.gauge("rocksdb_get_hit_l0",statistics.getTickerCount(TickerType.GET_HIT_L0));
        meterRegistry.gauge("rocksdb_get_hit_l1",statistics.getTickerCount(TickerType.GET_HIT_L1));
        meterRegistry.gauge("rocksdb_get_hit_l2_and_up",statistics.getTickerCount(TickerType.GET_HIT_L2_AND_UP));
        meterRegistry.gauge("rocksdb_blob_db_num_put",statistics.getTickerCount(TickerType.BLOB_DB_NUM_PUT));

    }

    public RocksdbCacheProvider(MeterRegistry meterRegistry, ScheduledExecutorService storageThreadPool) throws IOException {

        try {
            RocksDB.loadLibrary();
        } catch (Throwable t) {
            throw new IOException("Failed to load RocksDB JNI library", t);
        }

        if(storageThreadPool==null) {
            this.storageThreadPool = Executors.newScheduledThreadPool(1);
        }else{
            this.storageThreadPool=storageThreadPool;
        }



        this.optionSync = new WriteOptions();
        this.optionDontSync = new WriteOptions();
        this.optionCache = new ReadOptions();
        this.optionDontCache = new ReadOptions();
        this.emptyBatch = new WriteBatch();

        String dbFilePath = "rocksdb.conf";
        DBOptions dbOptions = new DBOptions();
        dbOptions.setCreateIfMissing(true);
        final List<ColumnFamilyDescriptor> cfDescs = new ArrayList<>();
        final List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
        try {

            File configFile = new File(dbFilePath);
            if(configFile.exists()) {
                OptionsUtil.loadOptionsFromFile(dbFilePath, Env.getDefault(), dbOptions, cfDescs, false);
            }
            log.info("Load options from configFile({}), CF.size={},dbConfig={}", dbFilePath,
                    cfDescs.size(), dbOptions);
            if (log.isDebugEnabled()) {
                for (ColumnFamilyDescriptor cfDescriptor : cfDescs) {
                    log.debug("CF={},Options={}", cfDescriptor.getName(), cfDescriptor.getOptions().toString());
                }
            }
            // Configure file path

            //TODO enable blob and compaction style of fifo store from DBOptions
//            options.setEnableBlobFiles(true);
//            options.setCreateIfMissing(true);
////        options.setTtl()
//            options.setCompactionStyle(CompactionStyle.FIFO);  // only one level, level 0
//            options.setMaxTableFilesSizeFIFO(10);  // sst file max size


            String logPath =  "cache/rocksdb_log_path";
            String subPath = "subpath";
            String basePath = "cache/lank8s";
            boolean readOnly = false;
            Path logPathSetting = FileSystems.getDefault().getPath(logPath, subPath);
            if (!logPathSetting.toFile().exists()) {
                Files.createDirectories(logPathSetting);
            }
            log.info("RocksDB<{}> log path: {}", subPath, logPathSetting);

            Statistics statistics = new Statistics();
            statistics.setStatsLevel(StatsLevel.ALL);

            dbOptions.setDbLogDir(logPathSetting.toString());
            dbOptions.setStatistics(statistics);

            if(meterRegistry!=null) {
                this.storageThreadPool.scheduleAtFixedRate(() -> {
                    writeMetrics(meterRegistry, statistics);
                }, 15, 15, TimeUnit.SECONDS);
            }

            String path = FileSystems.getDefault().getPath(basePath, subPath).toFile().toString();
            File file = new File(path);
            if (!file.exists()){
                file.mkdirs();
            }

            if (readOnly) {
                db = RocksDB.openReadOnly(dbOptions, path, cfDescs, cfHandles);
            } else {
                if(configFile.exists()) {
                    db = RocksDB.open(dbOptions, path, cfDescs, cfHandles);
                }else{
                    Options options = new Options();
                    options.setCreateIfMissing(true);
                    options.setStatistics(statistics);
                    options.setEnableBlobGarbageCollection(true);
                    options.setBlobCompressionType(CompressionType.LZ4_COMPRESSION);
                    options.setBlobFileSize(128*1024);
                    options.setEnableBlobFiles(true);
                    //TODO  rocksdb remote compaction
                    // rocksdb  kv fenli
//                    options.setRowCache();
//                    options.setTtl(60*60*24*30); //ttl second
//                    options.setMinWriteBufferNumberToMerge()
//                    options.setMaxWriteBufferNumberToMaintain()
//                    options.setMemtableHugePageSize()
//                    options.setEnableBlobFiles()
//                    configLog(options);
                            db = RocksDB.open(options,path);
                }
            }
        } catch (RocksDBException e) {
            throw new IOException("Error open RocksDB database", e);
        }

        optionSync.setSync(true);
        optionDontSync.setSync(false);

        optionCache.setFillCache(true);
        optionDontCache.setFillCache(false);

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
                db.put(key, value);
                cf.complete(null);
            }catch (RocksDBException e){
                log.error("put failed!",e);
                cf.completeExceptionally(e);
            }
        });
        return cf;
    }

    @Override
    public void delete(byte[] key) throws IOException {
        try {
            db.delete(key);
        }catch (RocksDBException e){
            throw new IOException("Error in RocksDB delete", e);
        }
    }

    @Override
    public byte[] get(byte[] key) throws IOException {
        try {
            return db.get(key);
        }catch (RocksDBException e){
            throw new IOException("Error in RocksDB get", e);
        }
    }

    @Override
    public void sync() throws IOException {
        try {
            db.write(optionSync, emptyBatch);
        } catch (RocksDBException e) {
            throw new IOException("Error in RocksDB sync", e);
        }
    }

    @Override
    public Set<String> keys() {
//        RocksIterator rocksIterator = db.newIterator();
        return Collections.emptySet();
    }

    @Override
    public void close() throws IOException {
        db.close();
    }
}
