package com.danmodan.adventofcode.jedis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;

@Configuration
public class JedisConfig {

    private final String host;
    private final int port;

    public JedisConfig(
        @Value("${jedis.host:localhost}") String host, 
        @Value("${jedis.port:6379}") int port) {

        this.host = host;
        this.port = port;
    }

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(host, port);
    }
}