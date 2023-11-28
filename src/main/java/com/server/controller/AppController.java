package com.server.controller;


import com.server.pojo.vo.AppAddVo;
import com.server.pojo.vo.AppDetailsVo;
import com.server.pojo.vo.AppVo;
import com.server.service.AppServiceImpl;
import com.server.util.CmdUtil;
import io.swagger.annotations.Api;
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


    @GetMapping("/test")
    public String test(String shell) throws Exception {
        return CmdUtil.linuxExecShell(shell);
    }


    /**
     * 查询app列表
     *
     * @return
     */
    @GetMapping("/getAppList")
    public List<AppVo> getAppList() throws Exception {
        return appService.getAppList();
    }

    /**
     * 发布版本
     */
    @GetMapping("/publishApp")
    public Boolean publishApp(String appName, String publishVersion) throws Exception {
        return appService.publishApp(appName, publishVersion);
    }


    /**
     * 获取版本列表
     */
    @GetMapping("/getAppVersions")
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
    public Boolean clearAppVersions(String appName, String time) {
        return appService.clearAppVersions(appName, time);
    }

    /**
     * 发布定时任务
     */
    @GetMapping("/publishTask")
    public Boolean publishTask(String appName, String publishVersion, String time) throws Exception {
        log.info("publishTask: appName: {},publishVersion: {}", appName, publishVersion);
        return appService.publishTask(appName, publishVersion, time);
    }

    /**
     * 是否存在定时任务
     */
    @GetMapping("/existTask")
    public Boolean existTask(String appName) {
        return appService.existTask(appName);
    }

    /**
     * 取消定时任务
     */
    @GetMapping("/cancelTask")
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
    public String testPort(String server,String port) throws Exception {
        appService.testPort(server,port);
        return "该端口未被使用";
    }

    /**
     * 添加实例
     */
    @PostMapping("/addOrUpdateApp")
    public Boolean addOrUpdateApp(@RequestBody AppAddVo appAddVo) throws Exception {
        return appService.addOrUpdateApp(appAddVo);
    }

    /**
     * 获取初始化配置
     */
    @GetMapping("/getInitInfo")
    public AppAddVo getInitInfo(String appName) throws Exception {
        return appService.getInitInfo(appName);
    }

    /**
     * 获取服务器集群列表
     */
    @GetMapping("/getServerIps")
    public List<String> getServerIps() throws Exception {
        return appService.getServerIps();
    }

    /**
     * 删除实例配置
     */
    @GetMapping("/delApp")
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
    public List<AppDetailsVo> getAppDetailsList(String appName) throws Exception {
        return appService.getAppDetailsList(appName);
    }


    /**
     * 下线实例
     */
    @GetMapping("/tapeOutApp")
    public Boolean tapeOutApp(Long appId) throws Exception {
        return appService.tapeOutApp(appId);
    }

    /**
     * 启动实例
     */
    @GetMapping("/runApp")
    public Boolean runApp(Long appId) throws Exception {
        return appService.runApp(appId);
    }


}
