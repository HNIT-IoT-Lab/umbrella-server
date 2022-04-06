package com.volunteer.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: 梁峰源
 * @date: 2022/4/2 20:32
 * Description: 用来限制同一时间段开锁人数
 */
@Inherited
@Documented
@Target({ElementType.FIELD,ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {
    /**
     * 标识 指定sec时间段内的访问次数限制，默认每个时间段只能有一个用户访问
     */
    int limit() default 1;

    /**
     * 标识 时间段
     */
    int sec() default 10;

    /**
     * 时间单位，默认秒
     */
    TimeUnit TIME_UNIT() default TimeUnit.SECONDS;
}
