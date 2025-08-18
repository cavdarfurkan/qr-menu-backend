package com.furkancavdar.qrmenu.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.queue.BuildMenuJobDto;
import io.lettuce.core.ClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // This is needed to avoid Redis CONFIG command issues in some environments
//    @Bean
//    public ConfigureRedisAction configureRedisAction() {
//        return ConfigureRedisAction.NO_OP;
//    }

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration("localhost", 6379);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMinutes(0))
                .clientOptions(
                        ClientOptions.builder()
                                .autoReconnect(true)
                                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                                .build()
                )
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, BuildMenuJobDto> buildMenuJobRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, BuildMenuJobDto> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        Jackson2JsonRedisSerializer<BuildMenuJobDto> serializer = new Jackson2JsonRedisSerializer<>(mapper, BuildMenuJobDto.class);

        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(serializer);
        t.afterPropertiesSet();
        return t;
    }

//    @Bean(name = "queueConsumerClient")
//    public RedisTemplate<String, String> queueConsumerRedisTemplate(LettuceConnectionFactory connectionFactory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//
//        connectionFactory.afterPropertiesSet();
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
}