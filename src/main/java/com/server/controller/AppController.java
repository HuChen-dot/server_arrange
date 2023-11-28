package com.server.controller;


import com.server.pojo.vo.AppAddVo;
import com.server.pojo.vo.AppDetailsVo;
import com.server.pojo.vo.AppVo;
import com.server.service.AppServiceImpl;
import com.server.util.CmdUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/app")
@Api(tags = "app管理")
@Slf4j
public class AppController {


    @Autowired
    private AppServiceImpl appService;


    /**
     * 运行脚本
     * @param ip
     * @param shell
     * @return
     * @throws Exception
     */
    @GetMapping("/runShell")
    @ApiOperation("运行脚本")
    public String test(String ip,String shell) throws Exception {
        return CmdUtil.linuxExecShell(ip,shell);
    }


    /**
     * 查询app列表
     *
     * @return
     */
    @GetMapping("/getAppList")
    @ApiOperation("查询app列表")
    public List<AppVo> getAppList() throws Exception {
        return appService.getAppList();
    }

    /**
     * 发布版本
     */
    @GetMapping("/publishApp")
    @ApiOperation("发布版本")
    public Boolean publishApp(String appName, String publishVersion) throws Exception {
        return appService.publishApp(appName, publishVersion);
    }


    /**
     * 获取版本列表
     */
    @GetMapping("/getAppVersions")
    @ApiOperation("获取版本列表")
    public List<String> getAppVersions(String appName) {
        return appService.getAppVersions(appName);
    }


    /**
     * 清除版本列表
     *
     * @param appName
     * @param time
     * @return
     */
    @GetMapping("/clearAppVersions")
    @ApiOperation("清除版本列表")
    public Boolean clearAppVersions(String appName, String time) {
        return appService.clearAppVersions(appName, time);
    }

    /**
     * 发布定时任务
     */
    @GetMapping("/publishTask")
    @ApiOperation("发布定时任务")
    public Boolean publishTask(String appName, String publishVersion, String time) throws Exception {
        log.info("publishTask: appName: {},publishVersion: {}", appName, publishVersion);
        return appService.publishTask(appName, publishVersion, time);
    }

    /**
     * 是否存在定时任务
     */
    @GetMapping("/existTask")
    @ApiOperation("是否存在定时任务")
    public Boolean existTask(String appName) {
        return appService.existTask(appName);
    }

    /**
     * 取消定时任务
     */
    @GetMapping("/cancelTask")
    @ApiOperation("取消定时任务")
    public Boolean cancelTask(String appName) {
        return appService.cancelTask(appName);
    }


    /**
     * 测试端口是否被使用
     * @param port
     * @return
     * @throws Exception
     */
    @GetMapping("/testPort")
    @ApiOperation("测试端口是否被使用")
    public String testPort(String server,String port) throws Exception {
        appService.testPort(server,port);
        return "该端口未被使用";
    }

    /**
     * 添加实例
     */
    @PostMapping("/addOrUpdateApp")
    @ApiOperation("添加实例")
    public Boolean addOrUpdateApp(@RequestBody AppAddVo appAddVo) throws Exception {
        return appService.addOrUpdateApp(appAddVo);
    }

    /**
     * 获取初始化配置
     */
    @GetMapping("/getInitInfo")
    @ApiOperation("获取初始化配置")
    public AppAddVo getInitInfo(String appName) throws Exception {
        return appService.getInitInfo(appName);
    }

    /**
     * 获取服务器集群列表
     */
    @GetMapping("/getServerIps")
    @ApiOperation("获取服务器集群列表")
    public List<String> getServerIps() throws Exception {
        return appService.getServerIps();
    }

    /**
     * 删除实例配置
     */
    @GetMapping("/delApp")
    @ApiOperation("删除实例配置")
    public Boolean delApp(Long appId) throws Exception {
        return appService.delApp(appId);
    }
    /**
     * 获取实例详情列表
     * @param appName
     * @return
     * @throws Exception
     */
    @GetMapping("/getAppDetailsList")
    @ApiOperation("获取实例详情列表")
    public List<AppDetailsVo> getAppDetailsList(String appName) throws Exception {
        return appService.getAppDetailsList(appName);
    }


    /**
     * 下线实例
     */
    @GetMapping("/tapeOutApp")
    @ApiOperation("下线实例")
    public Boolean tapeOutApp(Long appId) throws Exception {
        return appService.tapeOutApp(appId);
    }

    /**
     * 启动实例
     */
    @GetMapping("/runApp")
    @ApiOperation("启动实例")
    public Boolean runApp(Long appId) throws Exception {
        return appService.runApp(appId);
    }


}
