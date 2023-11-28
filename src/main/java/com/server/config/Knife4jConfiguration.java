package com.server.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


/**
 * @author lihao3
 * @date 2022/8/26 15:15
 */
@Configuration
@EnableKnife4j
@EnableSwagger2WebMvc
public class Knife4jConfiguration {


  @Bean
  public Docket defaultApi2() {
    Docket build = new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(
                    new ApiInfoBuilder()
                            .title("这是knife4j API ")
                            .description("# 这里记录服务端所有的接口的入参，出参等等信息")
                            .termsOfServiceUrl("10.99.10.122")
                            .contact(new Contact("陈虎","http://127.0.0.1","1024065216@qq.com"))
                            .version("2.6")
                            .build())
            .select()
            // 这里指定Controller扫描包路径
            .apis(RequestHandlerSelectors.basePackage("com.server.controller"))
            .paths(PathSelectors.any())
            .build();
    return build;
  }
}
