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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.danmodan.adventofcode.common.annotation.Delaeble;
import com.danmodan.adventofcode.common.annotation.Loggable;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Aspect
    @Component
    class Pointcuts {

        @Pointcut("@annotation(loggable)")
        public void loggable(Loggable loggable) {
        }

        @Pointcut("@annotation(delaeble)")
        public void delaeble(Delaeble delaeble) {
        }
    }

    @Aspect
    @Component
    class Advices {

        @Order(100)
        @Around(
            value = "AspectConfig.Pointcuts.loggable(loggable)", 
            argNames = "loggable")
        public Object logInputOutput(ProceedingJoinPoint jp, Loggable loggable) throws Throwable {

            String targetClassName = jp.getTarget().getClass().getName();
            String shortSignature = jp.getSignature().toShortString();
            String args = Stream.of(jp.getArgs()).map(Objects::toString).collect(Collectors.joining(","));
            Level currentLevel = Level.parse(loggable.level());
            Logger logger = Logger.getLogger(targetClassName);

            logger.log(currentLevel, () -> String.format("%s -> %s", shortSignature, args));
            Object result = jp.proceed();
            logger.log(currentLevel, () -> String.format("%s <- %s", shortSignature, result));

            return result;
        }

        @Order(101)
        @AfterThrowing(
            value = "AspectConfig.Pointcuts.loggable(loggable)", 
            throwing = "e", 
            argNames = "loggable,e")
        public void logError(JoinPoint jp, Loggable loggable, Exception e) {

            String targetClassName = jp.getTarget().getClass().getName();
            String shortSignature = jp.getSignature().toShortString();
            String args = Stream.of(jp.getArgs()).map(Objects::toString).collect(Collectors.joining(","));
            Logger logger = Logger.getLogger(targetClassName);

            logger.log(Level.SEVERE, e, () -> String.format("%s -> %s", shortSignature, args));
        }

        @Order(200)
        @Around(
            value = "AspectConfig.Pointcuts.delaeble(delaeble)",
            argNames = "delaeble")
        public Object delayMethod(ProceedingJoinPoint jp, Delaeble delaeble) throws Throwable {

            long milisseconds = delaeble.value();
            byte location = delaeble.location();

            if((location & 1) != 0) {
                Thread.sleep(milisseconds);
            }

            Object result = jp.proceed();

            if((location & 2) != 0) {
                Thread.sleep(milisseconds);
            }

            return result;
        }
    }
}