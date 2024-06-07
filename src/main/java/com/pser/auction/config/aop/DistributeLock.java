package com.pser.auction.config.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {
    String key();

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    long waitTime() default 1000L;

    long leaseTime() default 1000L;
}