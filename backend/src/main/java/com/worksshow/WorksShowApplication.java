package com.worksshow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WorksShow 后端启动类
 * <p>
 * 负责初始化 Spring 容器,扫描 mapper 包下的 Mybatis-Plus 接口。
 *
 * @author WorksShow
 */
@SpringBootApplication
@MapperScan("com.worksshow.mapper") // 扫描 Mapper 接口,免逐个加 @Mapper
@EnableScheduling                    // 启用定时任务(验证码过期清理等)
public class WorksShowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorksShowApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  WorksShow 后端启动成功\n" +
                "  接口前缀: http://localhost:8080/api\n" +
                "========================================\n");
    }
}
