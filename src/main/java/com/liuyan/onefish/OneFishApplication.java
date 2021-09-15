package com.liuyan.onefish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * springboot启动类
 * @author Administrator
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.liuyan.onefish.*")
public class OneFishApplication {
    private final static Logger logger = LoggerFactory.getLogger(OneFishApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OneFishApplication.class, args);
        logger.info("hello springboot !");
    }

}
