package com.ecommerce.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(BackendApplication.class, args);

		Environment env = ctx.getEnvironment();
		String port = env.getProperty("server.port", "7687");

		String banner = """
				\n
				================================================
				  🚀  E-Commerce Backend 啟動成功！
				  ► API 位址：http://localhost:%s/api
				  ► 健康檢查：http://localhost:%s/api/health
				  ► 前端位址：http://localhost:5173
				================================================
				""".formatted(port, port);

		System.out.println(banner);
	}

}
