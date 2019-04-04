package com.robot.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @ClassName ${ClassName}
 * @Description:
 * @Author 亮亮
 * @Date 2019/3/3015:06
 * @Version
 **/
@Configuration
@ImportResource({"classpath*:hsf-robot.xml"})
public class HsfConfig {
    static {
        System.out.println("创建bean。xml");
    }

}
