package com.lagou.cl.mvcframework.util;


public class FirstCharUtils {

	/**
	 * 首字母转小写
	 *
	 * @param s
	 * @return
	 */
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return s;
		} else {
			return Character.toLowerCase(s.charAt(0)) + s.substring(1);
		}
	}


	/**
	 * 首字母转大写
	 *
	 * @param s
	 * @return
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0))) {
			return s;
		} else {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		}
	}
}
