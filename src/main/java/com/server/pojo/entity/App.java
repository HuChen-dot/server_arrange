package com.server.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 
* @author chenhu
* @date 2023-11-20 14:47:02
*/
@Data
public class App implements Serializable {

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
     * 是否删除(0:未删除，1：已删除）
     */
    private byte del;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人
     */
    private String updateBy;
    /**
     * 修改时间
     */
    private Date updateTime;

}
