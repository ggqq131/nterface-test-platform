package com.lagou.cl.mvcframework.annotation;

import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouRespoinstry {

	String value() default "";
}
