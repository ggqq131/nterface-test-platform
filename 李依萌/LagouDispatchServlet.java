package com.lagou.cl.mvcframework;

import com.lagou.cl.mvcframework.annotation.*;
import com.lagou.cl.mvcframework.pojo.Handler;
import com.lagou.cl.mvcframework.util.FirstCharUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LagouDispatchServlet extends HttpServlet {

	private Properties properties = new Properties();
	private List<String> classNames = new ArrayList<>();
	private Map<String, Object> ioc = new HashMap<>();
	//	private Map<String, Method> handlerMapping = new HashMap<>();
	private List<Handler> handlerList = new ArrayList<>();

	/**
	 * 1.加载配置文件，springMvc.properties
	 * <p>
	 * 2.扫描注解
	 * <p>
	 * 3.初始化Bean，添加到IOC容器中
	 * <p>
	 * 4.bean对象之间的依赖
	 * <p>
	 * 5.初始化相关组件，构造一个handlerMapping，将配置好的url和method关联
	 * <p>
	 * 6.等待请求，初始化完成
	 *
	 * @param config
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		//1.加载配置文件，springMvc.properties
		doLoadConfig(config.getInitParameter("contextConfigLocation"));

		//2.扫描注解
		doScan(properties.getProperty("scanPackage"));

		//3.初始化Bean，添加到IOC容器中
		doInstance();

		//4.bean对象之间的依赖
		doAutowired();

		//5.初始化相关组件，构造一个handlerMapping，将配置好的url和method关联
		initHandlerMapping();

		System.out.println("初始化完成.........");
	}


	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");

		Handler handler = getHandler(req);

		if (null == handler) {
			resp.getWriter().write("404");
			return;
		}

		Method method = handler.getMethod();
		Map<String, Integer> paramIndexMap = handler.getParamIndexMap();

		//获取方法所有参数的类型
		Class<?>[] parameterTypes = method.getParameterTypes();

		Object[] params = new Object[parameterTypes.length];

		//获取所有参数
		Map<String, String[]> parameterMap = req.getParameterMap();
		for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
			String value = StringUtils.join(param.getValue(), ",");
			value = new String(value.getBytes("ISO8859-1"), StandardCharsets.UTF_8);

			//方法的参数名称和请求参数名称相同
			if (paramIndexMap.containsKey(param.getKey())) {
				Integer index = paramIndexMap.get(param.getKey());
				params[index] = value;
			}
		}

		Integer reqIndex = paramIndexMap.get(HttpServletRequest.class.getSimpleName());
		Integer resIndex = paramIndexMap.get(HttpServletResponse.class.getSimpleName());

		params[reqIndex] = req;
		params[resIndex] = resp;

		Object result = null;
		try {
			//调用方法之前判断是否有权限
			if (!preHandler(handler, parameterMap)) {
				String username = Arrays.toString(parameterMap.get("username"));
				username = new String(username.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
				System.out.println(username + "无权访问");
				resp.getWriter().write(username + "无权访问");
				return;
			}


			result = method.invoke(handler.getController(), params);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		resp.getWriter().print(result);
	}

	/**
	 * @param handler      handler
	 * @param parameterMap 请求参数
	 * @return boolean
	 */
	private boolean preHandler(Handler handler, Map<String, String[]> parameterMap) {

		boolean isPass = false;

		//定义了允许访问的用户名称
		Set<String> names = handler.getSecurityUserName();

		//请求的用户名
		String[] usernames = parameterMap.get("username");

		for (String username : usernames) {
			try {
				username = new String(username.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			//true 包含，允许访问
			isPass = names.contains(username);
			break;
		}
		return isPass;
	}

	private Handler getHandler(HttpServletRequest req) {
		//获取URI
		String requestURI = req.getRequestURI();
		for (Handler handler : handlerList) {
			Matcher matcher = handler.getPattern().matcher(requestURI);
			if (matcher.matches()) {
				return handler;
			}
		}

		return null;
	}

	/**
	 * 初始化处理器映射器
	 */
	private void initHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}

		ioc.forEach((beanId, obj) -> {
			if (obj.getClass().isAnnotationPresent(LagouController.class)) {
				String classUri = "";
				if (obj.getClass().isAnnotationPresent(LagouRequestMapping.class)) {
					classUri = obj.getClass().getAnnotation(LagouRequestMapping.class).value();
				}

				Set<String> classAllowUserName = new HashSet<>();
				//判断controller类上是否有security注解
				if (obj.getClass().isAnnotationPresent(Security.class)) {
					String[] names = obj.getClass().getAnnotation(Security.class).value();
					classAllowUserName.addAll(Arrays.asList(names));
				}

				//获取类中的方法
				Method[] methods = obj.getClass().getMethods();
				for (Method method : methods) {
					//方法是否有LagouRequestMapping注解
					if (method.isAnnotationPresent(LagouRequestMapping.class)) {
						String methodUri = method.getAnnotation(LagouRequestMapping.class).value();

						String uri = classUri + methodUri;
						//handlerMapping.put(uri, method);

						//封装handler
						Handler handler = new Handler(obj, method, Pattern.compile(uri));

						//获取方法的参数
						Parameter[] parameters = method.getParameters();
						for (int i = 0; i < parameters.length; i++) {
							Parameter parameter = parameters[i];
							//普通类型
							Class<?> parameterType = parameter.getType();
							if (parameterType.equals(HttpServletRequest.class) || parameterType.equals(HttpServletResponse.class)) {

								//如果参数类型是这两个类型，存入名称HttpServletRequest和HttpServletResponse
								handler.getParamIndexMap().put(parameterType.getSimpleName(), i);
							} else {
								handler.getParamIndexMap().put(parameter.getName(), i);
							}
						}
						handlerList.add(handler);


						//判断方法上是否有security注解
						Set<String> methodAllowUserName = new HashSet<>();
						if (method.isAnnotationPresent(Security.class)) {
							String[] names = method.getAnnotation(Security.class).value();
							methodAllowUserName.addAll(Arrays.asList(names));
						}

						//将该controller中所有的security注解的值，放入Map
						handler.getSecurityUserName().addAll(classAllowUserName);
						handler.getSecurityUserName().addAll(methodAllowUserName);
					}
				}


			}
		});
	}

	private void doAutowired() {

		if (ioc.isEmpty()) {
			return;
		}

		ioc.forEach((beanId, obj) -> {
			try {
				//获取该对象中的所有字段
				Field[] declaredFields = obj.getClass().getDeclaredFields();
				for (Field field : declaredFields) {
					if (field.isAnnotationPresent(LagouAutowired.class)) {
						field.setAccessible(true);
						String beanName = field.getAnnotation(LagouAutowired.class).value();
						if (null == beanName || "".equals(beanName)) {
							//获取当前字段的类型的名称
							beanName = field.getType().getName();
						}
						//赋值
						field.set(obj, ioc.get(beanName));
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});

	}

	private void doInstance() {

		if (classNames.isEmpty()) {
			return;
		}

		try {
			for (int i = 0; i < classNames.size(); i++) {

				String className = classNames.get(i);

				Class<?> aClass = Class.forName(className);

				//实例化Controller
				if (aClass.isAnnotationPresent(LagouController.class)) {
					String simpleName = aClass.getSimpleName();
					String toLowerCaseFirstOne = FirstCharUtils.toLowerCaseFirstOne(simpleName);

					//实例化
					Object newInstance = aClass.newInstance();
					ioc.put(toLowerCaseFirstOne, newInstance);

				}

				if (aClass.isAnnotationPresent(LagouService.class)) {

					String beanName = null;
					beanName = aClass.getAnnotation(LagouService.class).value();
					if (null != beanName && !"".equals(beanName)) {
						ioc.put(beanName, aClass.newInstance());
					} else {
						beanName = aClass.getSimpleName();
						beanName = FirstCharUtils.toLowerCaseFirstOne(beanName);
						ioc.put(beanName, aClass.newInstance());
					}

					//面向接口开发，此时以接口名称为接口的全限定类名称,向IOC里放一份,便于根据接口类型注入
					Class<?>[] interfaces = aClass.getInterfaces();
					for (int i1 = 0; i1 < interfaces.length; i1++) {
						Class<?> aClassInterface = interfaces[i1];
						ioc.put(aClassInterface.getName(), aClass.newInstance());
					}
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}


	}

	private void doScan(String basePackage) {

		String basePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + basePackage.replaceAll("\\.", "/");
		File pack = new File(basePath);

		File[] files = pack.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				doScan(basePackage + "." + file.getName());
			} else if (file.getName().endsWith(".class")) {
				String className = basePackage + "." + file.getName().replaceAll("\\.class", "");
				classNames.add(className);
			}
		}

	}

	private void doLoadConfig(String contextConfigLocation) {

		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
