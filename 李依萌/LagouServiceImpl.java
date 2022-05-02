package com.lagou.cl.demo.service.ipml;

import com.lagou.cl.demo.service.LagouServices;
import com.lagou.cl.mvcframework.annotation.LagouService;


@LagouService("lagouService")
public class LagouServiceImpl implements LagouServices {

	@Override
	public String query(String name) {
		return name;
	}
}
