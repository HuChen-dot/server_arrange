package com.server.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AppVo {

    /**
     * 服务器
     */
    private String server;

    /**
     * app名称
     */
    @ApiModelProperty(value = "app名称")
    private String appName;


    /**
     * 实例数
     */
    @ApiModelProperty(value = "实例数")
    private Integer appSize = 0;

    /**
     * 在线实例数
     */
    @ApiModelProperty(value = "在线实例数")
    private Integer onLineSize = 0;

    /**
     * 当前执行版本
     */
    @ApiModelProperty(value = "当前执行版本")
    private String nowRunVersion;

    /**
     * app状态
     */
    @ApiModelProperty(value = "app状态")
    private String status;


    @ApiModelProperty(value = "是否展示取消任务按钮")
    private boolean showCancelTask = false;

}
