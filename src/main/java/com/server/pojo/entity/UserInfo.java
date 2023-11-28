package com.server.pojo.entity;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiOperation("UserInfo实体类")
public class UserInfo implements Serializable {
    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("userName")
    private String userName;

    @ApiModelProperty("loginName")
    private String loginName;

    @ApiModelProperty("companyId")
    private Long companyId;

    @ApiModelProperty("companyName")
    private String companyName;

    @ApiModelProperty("deptName")
    private String deptName;

    @ApiModelProperty("deptId")
    private Long deptId;

    @ApiModelProperty("workCode")
    private String workCode;

    @ApiModelProperty("email")
    private String email;

    @ApiModelProperty("phone")
    private String phone;

    @ApiModelProperty("zoneCode")
    private String zoneCode;

    @ApiModelProperty("是否是外部用户，外部用户：true, 内部用户：false")
    private boolean externalUser = false;

}
