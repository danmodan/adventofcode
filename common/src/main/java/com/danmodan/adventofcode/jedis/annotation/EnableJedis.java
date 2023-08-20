package com.danmodan.adventofcode.jedis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.danmodan.adventofcode.jedis.config.JedisConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JedisConfig.class)
public @interface EnableJedis {
}
