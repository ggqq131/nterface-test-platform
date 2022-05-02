package com.lagou.cl.demo.controller;

import com.lagou.cl.demo.service.LagouServices;
import com.lagou.cl.mvcframework.annotation.LagouAutowired;
import com.lagou.cl.mvcframework.annotation.LagouController;
import com.lagou.cl.mvcframework.annotation.LagouRequestMapping;
import com.lagou.cl.mvcframework.annotation.Security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@LagouController
@LagouRequestMapping("/demo")
@Security({"程林", "李沁"})
public class LagouControllerr {

	@LagouAutowired
	private LagouServices lagouServices;


	@LagouRequestMapping("/query01")
	@Security({"cljava", "刘亦菲"})
	public String query01(HttpServletRequest request, HttpServletResponse response, String username) {
		return lagouServices.query(username);
	}

	@LagouRequestMapping("/query02")
	@Security({"clj2ee", "赵丽颖"})
	public String query02(HttpServletRequest request, HttpServletResponse response, String username) {
		return lagouServices.query(username);
	}
}
