package com.lagou.cl.mvcframework.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Security {

	/**
	 * 值为用户名，用于拦截，表明哪些用户可以访问
	 * @return
	 */
	String[] value() default {};
}
