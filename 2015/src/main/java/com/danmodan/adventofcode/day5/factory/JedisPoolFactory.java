package com.danmodan.adventofcode.day5.factory;

import redis.clients.jedis.JedisPool;

public class JedisPoolFactory {

    private JedisPoolFactory() {
    }

    private static JedisPool INSTANCE;

    public static JedisPool getInstance() {

        if (INSTANCE != null) {
            return INSTANCE;
        }

        synchronized (JedisPoolFactory.class) {

            if (INSTANCE == null) {
                INSTANCE = new JedisPool();
            }

            return INSTANCE;
        }
    }
}
