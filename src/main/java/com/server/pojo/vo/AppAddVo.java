package com.server.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
* 
* @author chenhu
* @date 2023-11-20 14:47:02
*/
@Data
public class AppAddVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 服务名
     */
    private String appName;

    /**
     * 服务器
     */
    private String server;

    /**
     * 端口号
     */
    private String port;
    /**
     * sh启动文件
     */
    private String shFile;

    /**
     * 启动等待时间
     */
    private String awaitStartTime;

    /**
     * jvm参数
     */
    private String jvmArgs;

}
