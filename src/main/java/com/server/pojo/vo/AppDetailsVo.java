package com.server.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
* 
* @author chenhu
* @date 2023-11-20 14:47:02
*/
@Data
public class AppDetailsVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 服务器
     */
    private String server;

    /**
     * 服务名
     */
    private String appName;
    /**
     * 端口号
     */
    private String port;
    /**
     * sh启动文件
     */
    private String shFile;
    /**
     * 当前运行版本
     */
    private String runVersion;
    /**
     * 启动等待时间
     */
    private String awaitStartTime;

    /**
     * jvm参数
     */
    private String jvmArgs;


    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 运行状态（0：运行中，1：重启中，2：离线）
     */
    private Integer runStatus;

    /**
     * 运行状态（0：运行中，1：重启中，2：离线）
     */
    private String runStatusName;

}
