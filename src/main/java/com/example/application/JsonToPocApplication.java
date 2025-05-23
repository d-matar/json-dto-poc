package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan
//@EnableJpaRepositories(considerNestedRepositories = true)
public class JsonToPocApplication extends SpringBootServletInitializer {

	
	 public static void main(String[] args) {
	        SpringApplication.run(JsonToPocApplication.class, args);
	    }

	    @Override
	    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(JsonToPocApplication.class);
	    }
}
