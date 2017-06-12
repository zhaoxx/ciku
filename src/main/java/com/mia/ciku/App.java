package com.mia.ciku;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication(scanBasePackages = "com.mia.ciku")
@EnableAspectJAutoProxy
//@PropertySources(
//{
//	/* 标准配置 */
//	@PropertySource(value = "classpath:config.properties", ignoreResourceNotFound = true)
//})
@Configuration
@EnableAutoConfiguration
@ImportResource(
{ "classpath:spring.xml"})
@EnableScheduling
public class App {
	public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        //设置默认语言为英文，JSON 序列化反序列化时，月份使用英文。
        Locale.setDefault(Locale.US);

        context = SpringApplication.run(App.class, args);
    }
}
