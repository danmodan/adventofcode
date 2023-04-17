package com.danmodan.adventofcode.common.config;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import com.danmodan.adventofcode.common.annotation.Delaeble;
import com.danmodan.adventofcode.common.annotation.Loggable;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Aspect
    @Component
    static class Pointcuts {

        @Pointcut("@annotation(loggable)")
        public void loggable(Loggable loggable) {
        }

        @Pointcut("@annotation(delaeble)")
        public void delaeble(Delaeble delaeble) {
        }
    }

    @Aspect
    @Component
    static class Advices {

        @Around(
            value = "com.danmodan.adventofcode.config.AspectConfig.Pointcuts.loggable(loggable)", 
            argNames = "loggable")
        public Object logInputOutput(ProceedingJoinPoint jp, Loggable loggable) throws Throwable {

            String targetClassName = jp.getTarget().getClass().getName();
            String shortSignature = jp.getSignature().toShortString();
            String args = Stream.of(jp.getArgs()).map(Objects::toString).collect(Collectors.joining(","));
            Level currentLevel = Level.parse(loggable.level());
            Logger logger = Logger.getLogger(targetClassName);

            logger.log(currentLevel, String.format("%s -> %s", shortSignature, args));
            Object result = jp.proceed();
            logger.log(currentLevel, String.format("%s <- %s", shortSignature, result));

            return result;
        }

        @AfterThrowing(
            value = "com.danmodan.adventofcode.config.AspectConfig.Pointcuts.loggable(loggable)", 
            throwing = "e", 
            argNames = "loggable,e")
        public void logError(JoinPoint jp, Loggable loggable, Exception e) {

            String targetClassName = jp.getTarget().getClass().getName();
            String shortSignature = jp.getSignature().toShortString();
            String args = Stream.of(jp.getArgs()).map(Objects::toString).collect(Collectors.joining(","));
            Logger logger = Logger.getLogger(targetClassName);

            logger.log(Level.SEVERE, String.format("%s -> %s", shortSignature, args), e);
        }

    }
}
