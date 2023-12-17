package cn.lank8s.springboot.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;


@Configuration
public class RedisConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RedisConnection redisConnection(RedisConnectionFactory redisConnectionFactory) {
        return redisConnectionFactory.getConnection();
    }

    /**
     * GenericObjectPoolConfig 连接池配置
     */
    @Bean
    public GenericObjectPoolConfig genericObjectPoolConfig(RedisProperties redisProperties) {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        if(redisProperties.getLettuce()!=null && redisProperties.getLettuce().getPool()!=null) {
            genericObjectPoolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
            genericObjectPoolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
            genericObjectPoolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
            genericObjectPoolConfig.setMaxWaitMillis(redisProperties.getLettuce().getPool().getMaxWait().toMillis());
        }
        return genericObjectPoolConfig;
    }

    /**
     * 工厂创建了连接 但是一个单实例
     **/
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(GenericObjectPoolConfig genericObjectPoolConfig,RedisProperties redisProperties) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisProperties.getHost(),redisProperties.getPort());
        redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig);

        if(redisProperties.getTimeout()!=null){
            builder = builder.commandTimeout(redisProperties.getTimeout());
        }

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,builder.build());
        return lettuceConnectionFactory;
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        org.redisson.spring.data.connection.RedissonConnectionFactory
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        //jackson将java对象转换成json对象。
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //设置了jackson序列化后 value数据会添加双引号
//        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

}
