package com.songjm.m2clean;

import com.songjm.m2clean.handle.RepositoryHandle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Title: com.songjm.m2clean.M2CleanApplication
 * <p>
 * Description: 应用主类
 * </p>
 *
 * @Author: songjm
 * @CreateTime: 2019/8/6 09:45
 */
@SpringBootApplication
public class M2CleanApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(M2CleanApplication.class, args);

		applicationContext.getBean(RepositoryHandle.class).handle();
	}

}
