package com.lagou.cl.mvcframework.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouRequestMapping {

	String value() default "";
}
