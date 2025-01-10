package com.example.rc4_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@Slf4j
public class MyConstants extends WebMvcConfigurationSupport {

    @Value("${imagepath}")
    private String imagepath;
    //静态资源目录,文件上传的相对目录（通过项目访问到本地的目录）
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);

        //                          项目的路径，/                                           file:本地目录
        //                          / 根目录， ** 该目录下的所有子目录，子文件                 classpath:项目根目录
        ApplicationHome applicationHome = new ApplicationHome(this.getClass());
//        String absolutePath = applicationHome.getDir().getParentFile()
//                .getParentFile().getAbsolutePath()+"\\src\\main\\resources\\image\\";
        registry.addResourceHandler("/image/**").addResourceLocations("file:"+imagepath);//配置本地文件夹
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        log.info("自定义静态资源目录、此处功能用于文件映射");
    }
}
