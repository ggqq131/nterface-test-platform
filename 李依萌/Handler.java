package com.lagou.cl.mvcframework.pojo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class Handler {

	/**
	 * invoke(obj,...)
	 */
	private Object controller;

	/**
	 * 方法对象
	 */
	private Method method;

	/**
	 * Uri支持正则
	 */
	private Pattern pattern;

	/**
	 * 方法参数的索引，目的参数绑定；key参数名称，参数的索引
	 */
	private Map<String, Integer> paramIndexMap;

	/**
	 * 允许访问该Handler的用户名集合
	 */
	private Set<String> securityUserName;

	public Handler(Object controller, Method method, Pattern pattern) {
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
		this.paramIndexMap = new HashMap<>();
		this.securityUserName = new HashSet<>();
	}

	public Set<String> getSecurityUserName() {
		return securityUserName;
	}

	public void setSecurityUserName(Set<String> securityUserName) {
		this.securityUserName = securityUserName;
	}

	public Object getController() {
		return controller;
	}

	public void setController(Object controller) {
		this.controller = controller;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Map<String, Integer> getParamIndexMap() {
		return paramIndexMap;
	}

	public void setParamIndexMap(Map<String, Integer> paramIndexMap) {
		this.paramIndexMap = paramIndexMap;
	}
}
