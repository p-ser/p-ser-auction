package com.pser.auction.config;

import com.pser.auction.Util;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@RequiredArgsConstructor
public class RedisConfig {
    private final Environment env;

    @Bean(initMethod = "start")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("bitnami/redis:7.0.15-debian-12-r17"))
                .withExposedPorts(Util.getIntProperty(env, "redis.port"))
                .withEnv("REDIS_PASSWORD", env.getProperty("redis.password"))
                .withReuse(true);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(GenericContainer<?> redisContainer) {
        Integer mappedPort = redisContainer.getMappedPort(Util.getIntProperty(env, "redis.port"));
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                redisContainer.getHost(),
                mappedPort
        );
        configuration.setPassword(env.getProperty("redis.password"));
        configuration.setDatabase(Util.getIntProperty(env, "redis.database"));
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(GenericContainer<?> redisContainer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory(redisContainer));
        return redisTemplate;
    }

    @Bean
    public RedissonClient redissonClient(GenericContainer<?> redisContainer) {
        Integer mappedPort = redisContainer.getMappedPort(Util.getIntProperty(env, "redis.port"));
        String address = "redis://%s:%s".formatted(redisContainer.getHost(), mappedPort);
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(address);
        singleServerConfig.setDatabase(Util.getIntProperty(env, "redis.database"));
        singleServerConfig.setPassword(env.getProperty("redis.password"));
        return Redisson.create(config);
    }
}
