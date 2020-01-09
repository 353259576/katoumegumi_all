package cn.katoumegumi.java.config;


import cn.katoumegumi.java.properties.RedisWsProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Data
@Slf4j
@Configuration
@EnableConfigurationProperties(value = {RedisWsProperties.class})
@ConditionalOnProperty(prefix = "megumi.redis",value = "enable",havingValue = "true")
public class RedisConfig {

    @Resource
    private RedisWsProperties redisWsProperties;

    @Bean
    @ConditionalOnMissingBean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(Environment environment){
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisWsProperties.getHost());
        redisStandaloneConfiguration.setPort(redisWsProperties.getPort());
        if(redisWsProperties.getPassword() != null){
            redisStandaloneConfiguration.setPassword(redisWsProperties.getPassword());
        }
        return redisStandaloneConfiguration;
    }

    /**
     * reids连接池
     * @param redisStandaloneConfiguration
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionFactory lettuceConnectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration){
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisSerializer stringRedisSerializer(){
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        return stringRedisSerializer;
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(){
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        return genericJackson2JsonRedisSerializer;
    }






    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer, GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }


    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer, GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer){
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }




    /**
     * spring redis缓存配置
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){

        RedisCacheManager.RedisCacheManagerBuilder redisCacheManagerBuilder  = RedisCacheManager.builder(redisConnectionFactory);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofSeconds(redisWsProperties.getDefultCacheTime()));
        redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
        redisCacheManagerBuilder.cacheDefaults(redisCacheConfiguration);

        Map<String,Integer> caches = redisWsProperties.getCaches();
        if(caches != null) {
            Set<Map.Entry<String, Integer>> set = caches.entrySet();
            Iterator<Map.Entry<String, Integer>> iterator = set.iterator();
            Set<String> cacheNames = new HashSet<>();
            Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                cacheNames.add(entry.getKey());
                configMap.put(entry.getKey(), redisCacheConfiguration.entryTtl(Duration.ofSeconds(entry.getValue())));
            }
            redisCacheManagerBuilder.initialCacheNames(cacheNames);
            redisCacheManagerBuilder.withInitialCacheConfigurations(configMap);
        }
        RedisCacheManager redisCacheManager = redisCacheManagerBuilder.build();
        return redisCacheManager;
    }

}
