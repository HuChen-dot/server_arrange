package com.server.service;

import cn.hutool.core.collection.CollectionUtil;
import com.server.exception.BusinessException;
import com.server.mapper.AppMapper;
import com.server.msg.WeChatService;
import com.server.pojo.entity.App;
import com.server.pojo.entity.UserInfo;
import com.server.pojo.vo.AppAddVo;
import com.server.pojo.vo.AppDetailsVo;
import com.server.pojo.vo.AppVo;
import com.server.util.CmdUtil;
import com.server.util.DateUtil;
import com.server.util.UserThreadLocal;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@Data
@ConfigurationProperties(prefix = "ftp")
public class AppServiceImpl {

    private String skyIp;

    private List<String> serverIps;

    private List<String> whitelist;

    private List<String> skipCatalogue;

    @Autowired
    private WeChatService weChatService;

    @Value("${spring.profiles.active}")
    private String active;

    @Autowired
    private AppMapper appMapper;

    private ThreadPoolExecutor restartAppPool = new ThreadPoolExecutor(2, 5, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));

    private final static Map<String, String> taskCheck = new HashMap<>();

    //创建任务调度线程池对象
    private static final ScheduledExecutorService taskTool = new ScheduledThreadPoolExecutor(8);

    private static final String rootDirectory = "/www/server";

    /**
     * 查询app列表
     *
     * @return
     */
    public List<AppVo> getAppList() throws Exception {
        List<String> appNames = getFileList(null, rootDirectory);

        List<AppVo> appList = new ArrayList<AppVo>();

        for (String appName : appNames) {
            if (!whitelist.contains(appName)) {
                continue;
            }

            List<App> apps = appMapper.selectByAppName(appName);
            AppVo appVo = new AppVo();
            appVo.setAppName(appName);
            if (CollectionUtil.isEmpty(apps)) {
                appVo.setNowRunVersion("当前服务未添加实例");
                appList.add(appVo);
                continue;
            }
            appVo.setAppSize(apps.size());

            if ("test".equals(active)) {
                String info = CmdUtil.linuxExecShell(apps.get(0).getServer(), "ps -ef |grep " + appVo.getAppName());

                List<String> subdirectory = getFileList(null, rootDirectory + "/" + appVo.getAppName());

                String version = "";
                for (String s : subdirectory) {
                    if (info.indexOf(s) != -1) {
                        version = s;
                        break;
                    }
                }
                appVo.setNowRunVersion(version);
                if (!version.equals(apps.get(0).getRunVersion())) {
                    // 修正数据
                    UserInfo userInfo = UserThreadLocal.get();
                    updateVersion(null, appVo.getAppName(), version, userInfo.getUserName());
                }
            } else {
                appVo.setNowRunVersion(apps.get(0).getRunVersion());
            }
            appVo.setShowCancelTask(taskCheck.get(appName) != null);

            if (apps.size() == 1) {
                String info = CmdUtil.linuxExecShell(apps.get(0).getServer(), "ps -ef |grep " + appName);
                appVo.setOnLineSize(0);
                if (StringUtils.isEmpty(info) || info.indexOf("java") == -1) {
                    appVo.setStatus("离线");
                    appList.add(appVo);
                    continue;
                }
                String infoProt = CmdUtil.linuxExecShell(apps.get(0).getServer(), "netstat -tuln | grep " + apps.get(0).getPort());
                if (StringUtils.isEmpty(infoProt)) {
                    appVo.setStatus("发布中");
                    appList.add(appVo);
                    continue;
                }
                appVo.setOnLineSize(1);
                appVo.setStatus("在线");
                appList.add(appVo);
                continue;
            }

            // 在线实例数
            int onLineCount = 0;
            // 重启中实例数
            int issueCount = 0;
            for (App app : apps) {
                String info = CmdUtil.linuxExecShell(app.getServer(), "ps -ef |grep " + app.getPort());
                if (StringUtils.isEmpty(info) || info.indexOf("java") == -1) {
                    // 代表当前服务器在离线状态
                    continue;
                }
                String infoProt = CmdUtil.linuxExecShell(app.getServer(), "netstat -tuln | grep " + app.getPort());
                if (StringUtils.isEmpty(infoProt)) {
                    // 正在重启
                    issueCount++;
                    continue;
                }
                onLineCount++;

            }
            appVo.setOnLineSize(onLineCount);
            if (issueCount > 0) {
                appVo.setStatus(issueCount + "台实例正在发布");
            } else {
                appVo.setStatus(appVo.getOnLineSize() > 0 ? "在线" : "离线");
            }
            appList.add(appVo);
        }
        return appList;
    }


    /**
     * 发布版本
     */
    public Boolean publishApp(String appName, String publishVersion) throws Exception {
        List<App> apps = check(appName, publishVersion);

        for (App app : apps) {
            copyFile(app.getServer(), app.getAppName(), publishVersion);
        }


        UserInfo userInfo = UserThreadLocal.get();
        String phone = userInfo.getPhone();

        // 启动
        restartAppPool.execute(() -> {
            try {
                doStart(apps, appName, publishVersion, userInfo);
            } catch (Exception e) {
                LinkedHashMap<String, String> textMap = new LinkedHashMap<>();
                textMap.put("提交人：", userInfo.getUserName());
                textMap.put("项目名称：", appName);
                textMap.put("所属环境：", active);
                log.error("项目：{}切换版本失败", appName, e);
                sendErrorMsg(textMap, appName, phone);
            }
        });

        return true;
    }


    private void copyFile(String server, String appName, String publishVersion) throws Exception {
        // 如果当前是宿主机，则不进行操作
        String ifconfig = CmdUtil.linuxExecShell("ifconfig");
        if (ifconfig.indexOf(server) != -1) {
            // 不进行操作
            return;
        }


        String path = rootDirectory + "/" + appName + "/" + publishVersion + "/";
        // 先进行删除
        CmdUtil.linuxExecShell(server, "rm -rf " + rootDirectory + "/" + appName + "/" + publishVersion);


        // 在进行拷贝
        CmdUtil.linuxExecShell("ssh root@" + server + " mkdir -p " + path + "logs");

        List<String> fileList = getFileList(null, path);
        for (String s : fileList) {
            if (s.endsWith(".jar") || s.endsWith(".sh")) {
                CmdUtil.linuxExecShell("scp " + path + s + " root@" + server + ":" + path);
            }
        }
    }


    /**
     * 获取版本列表
     */
    public List<String> getAppVersions(String appName) {

        List<String> subdirectory = getFileList(null, rootDirectory + "/" + appName);
        List<Date> cacheList = new ArrayList<>();
        Map<String, String> cacheMap = new HashMap<>();


        for (String file : subdirectory) {
            if (skipCatalogue.contains(file)) {
                continue;
            }
            try {
                String s1 = file.replace("~", " ").replace(".", ":");
                cacheMap.put(s1, file);
                cacheList.add(DateUtil.string2Date(s1));
            } catch (Exception e) {
                throw new BusinessException("错误的文件或目录：" + file);
            }
        }


        List<Date> collect = cacheList.stream().sorted().collect(Collectors.toList());

        List<String> items = new ArrayList<>();

        for (int i = (collect.size() - 1); i >= 0; i--) {
            items.add(cacheMap.get(DateUtil.date2String(collect.get(i))));
        }

        return items;
    }

    /**
     * 清除版本列表
     *
     * @param appName
     * @return
     */
    public boolean clearAppVersions(String appName, String time) {

        if (StringUtils.isEmpty(time)) {
            throw new BusinessException("请选择删除几天之前的版本");
        }

        List<App> apps = appMapper.selectByAppName(appName);

        if (CollectionUtil.isEmpty(apps)) {
            throw new BusinessException("请先配置实例在使用");
        }
        App app = apps.get(0);

        try {
            Date date = DateUtil.rollByDays(new Date(), -Integer.parseInt(time));

            List<String> subdirectory = getFileList(null, rootDirectory + "/" + appName);

            List<Date> cacheList = new ArrayList<>();
            Map<String, String> cacheMap = new HashMap<>();
            for (String file : subdirectory) {
                if (skipCatalogue.contains(file)) {
                    continue;
                }
                String s1 = file.replace("~", " ").replace(".", ":");
                cacheMap.put(s1, file);
                cacheList.add(DateUtil.string2Date(s1));
            }


            for (Date date1 : cacheList) {
                if (DateUtil.isBefore(date1, date)) {
                    String version = cacheMap.get(DateUtil.date2String(date1));
                    if (version.equals(app.getRunVersion())) {
                        continue;
                    }
                    // 删除
                    String shell = " rm -rf " + rootDirectory + "/" + appName + "/" + version + "/";
                    //删除宿主机的版本
                    CmdUtil.linuxExecShell(shell);
                    for (App app1 : apps) {
                        // 删除其他服务器的版本
                        CmdUtil.linuxExecShell(app1.getServer(), shell);
                    }

                }
            }
        } catch (Exception e) {
            log.error("清楚版本列表失败", e);
        }
        return true;
    }


    /**
     * 发布定时任务
     */
    public boolean publishTask(String appName, String publishVersion, String time) throws Exception {

        List<App> apps = check(appName, publishVersion);

        for (App app : apps) {
            copyFile(app.getServer(), app.getAppName(), publishVersion);
        }

        UserInfo userInfo = UserThreadLocal.get();
        String userName = userInfo.getUserName();
        String phone = userInfo.getPhone();

        cancelTask(appName);
        taskCheck.put(appName, publishVersion + "=" + time);

        LinkedHashMap<String, String> submitMap = new LinkedHashMap<>();
        submitMap.put("提交人：", userName);
        submitMap.put("项目名称：", appName);
        submitMap.put("所属环境：", active);
        submitMap.put("当前运行版本：", apps.get(0).getRunVersion());
        submitMap.put("发布的版本：", publishVersion);
        submitMap.put("预定发布时间：", time);
        log.debug("publishTask debug开始发送信息,textMap: {}, atMobiles:{}", submitMap, Arrays.asList(phone));
        weChatService.sendToWeChat("提交发版任务", submitMap, Arrays.asList(phone));

        taskTool.schedule(() -> {
            synchronized (taskCheck) {
                if (taskCheck.get(appName) == null || !taskCheck.get(appName).equals(publishVersion + "=" + time)) {
                    log.info("publishTask.schedule,任务已被取消, 应用名{}，触发时间：{}", appName, time);
                    return;
                }
                taskCheck.remove(appName);
            }

            LinkedHashMap<String, String> textMap = new LinkedHashMap<>();
            textMap.put("提交人：", userName);
            textMap.put("项目名称：", appName);
            textMap.put("所属环境：", active);
            try {
                weChatService.sendToWeChat("开始执行发版任务", textMap, Arrays.asList(phone));

                doStart(apps, appName, publishVersion, userInfo);

                textMap.put("当前运行版本：", publishVersion);
                textMap.put("执行状态：", "发布成功");
                weChatService.sendToWeChat("完成发版任务", textMap, Arrays.asList(phone));
            } catch (Exception e) {
                sendErrorMsg(textMap, appName, phone);
                log.error("定时任务执行失败", e);
            }

        }, (DateUtil.string2Date(time).getTime() - System.currentTimeMillis()), TimeUnit.MILLISECONDS);

        return true;
    }


    private void sendErrorMsg(LinkedHashMap<String, String> textMap, String appName, String phone) {
        String errorMsg = CmdUtil.linuxExecShell("tail -n -20 " + rootDirectory + "/" + appName + "/logs/info.log");
        if (errorMsg.length() > 1800) {
            errorMsg = errorMsg.substring((errorMsg.length() - 1800));
        }
        textMap.put("执行状态：", "发布失败");
        textMap.put("失败原因：", errorMsg);
        weChatService.sendToWeChat("发版任务失败", textMap, Arrays.asList(phone));

    }

    /**
     * 是否存在定时任务
     */
    public boolean existTask(String appName) {
        return taskCheck.get(appName) != null;
    }


    /**
     * 取消定时任务
     */
    public boolean cancelTask(String appName) {
        String info = taskCheck.get(appName);
        if (!StringUtils.isEmpty(info)) {
            UserInfo userInfo = UserThreadLocal.get();
            String userName = userInfo.getUserName();
            String phone = userInfo.getPhone();
            LinkedHashMap<String, String> textMap = new LinkedHashMap<>();
            textMap.put("提交人：", userName);
            textMap.put("项目名称：", appName);
            textMap.put("所属环境：", active);
            textMap.put("原定发布时间：", info.split("=")[1]);
            weChatService.sendToWeChat("取消发版任务", textMap, Arrays.asList(phone));
            taskCheck.remove(appName);
        }
        return true;
    }

    /**
     * 测试端口是否被使用
     *
     * @param port
     * @return
     * @throws Exception
     */
    public void testPort(String server, String port) {
        if (StringUtils.isEmpty(port)) {
            throw new BusinessException("端口不能为空");
        }
        if (StringUtils.isEmpty(server)) {
            throw new BusinessException("ip不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("server", server);
        param.put("port", port);
        param.put("del", 0);
        List<App> appList = appMapper.select(param);

        if (!CollectionUtil.isEmpty(appList)) {
            throw new BusinessException("该端口已被：{ " + appList.get(0).getAppName() + " }占用");
        }

        String infoProt1 = CmdUtil.linuxExecShell(server, "netstat -tuln | grep " + port);
        if (!StringUtils.isEmpty(infoProt1)) {
            throw new BusinessException("该端口已被占用");
        }
    }

    public List<AppDetailsVo> getAppDetailsList(String appName) {
        if (StringUtils.isEmpty(appName)) {
            throw new BusinessException("实例名为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("appName", appName);
        param.put("del", 0);
        List<App> appList = appMapper.select(param);
        if (CollectionUtil.isEmpty(appList)) {
            throw new BusinessException("当前服务未添加实例");
        }
        List<AppDetailsVo> appDetailsVoList = new ArrayList<>();
        for (App app : appList) {
            AppDetailsVo appDetailsVo = new AppDetailsVo();
            BeanUtils.copyProperties(app, appDetailsVo);
            String info = null;
            if (appList.size() == 1) {
                info = CmdUtil.linuxExecShell(app.getServer(), "ps -ef |grep " + app.getAppName());
            } else {
                info = CmdUtil.linuxExecShell(app.getServer(), "ps -ef |grep " + app.getPort());
            }
            if (StringUtils.isEmpty(info) || info.indexOf("java") == -1) {
                appDetailsVo.setRunStatus(2);
            } else {
                String infoProt = CmdUtil.linuxExecShell(app.getServer(), "netstat -tuln | grep " + app.getPort());
                if (StringUtils.isEmpty(infoProt)) {
                    // 正在重启
                    appDetailsVo.setRunStatus(1);
                } else {
                    appDetailsVo.setRunStatus(0);
                }
            }
            if (0 == appDetailsVo.getRunStatus()) {
                appDetailsVo.setRunStatusName("在线");
            } else if (1 == appDetailsVo.getRunStatus()) {
                appDetailsVo.setRunStatusName("发布中");
            } else {
                appDetailsVo.setRunStatusName("离线");
            }

            appDetailsVo.setCreateTime(DateUtil.date2String(app.getCreateTime()));
            appDetailsVoList.add(appDetailsVo);
        }
        return appDetailsVoList;
    }

    public boolean addOrUpdateApp(AppAddVo appAddVo) throws Exception {
        if (Objects.isNull(appAddVo.getId())) {
            return addApp(appAddVo);
        }
        return updateApp(appAddVo);
    }

    private boolean updateApp(AppAddVo appAddVo) {
        Map<String, Object> param = new HashMap<>();
        param.put("idIf", appAddVo.getId());
        param.put("jvmArgs", appAddVo.getJvmArgs());
        param.put("awaitStartTime", appAddVo.getAwaitStartTime());
        appMapper.update(param);
        return true;
    }

    private boolean addApp(AppAddVo appAddVo) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("appName", appAddVo.getAppName());
        param.put("del", 0);
        List<App> appList = appMapper.select(param);

        String version = null;
        if (!CollectionUtil.isEmpty(appList)) {
            testPort(appAddVo.getServer(), appAddVo.getPort());
            for (App app1 : appList) {
                if (!StringUtils.isEmpty(app1.getRunVersion())) {
                    version = app1.getRunVersion();
                    break;
                }
            }
        } else {
            String info = CmdUtil.linuxExecShell(appAddVo.getServer(), "ps -ef |grep " + appAddVo.getAppName());
            List<String> subdirectory = getFileList(null, rootDirectory + "/" + appAddVo.getAppName());
            for (String s : subdirectory) {
                if (info.indexOf(s) != -1) {
                    version = s;
                    break;
                }
            }
        }

        UserInfo userInfo = UserThreadLocal.get();
        String userName = userInfo.getUserName();
        App app = new App();
        BeanUtils.copyProperties(appAddVo, app);
        app.setCreateBy(userName);
        app.setUpdateBy(userName);
        if (!StringUtils.isEmpty(version)) {
            String info = CmdUtil.linuxExecShell(appAddVo.getServer(), "ps -ef |grep " + appAddVo.getAppName());
            if (info == null || info.indexOf(version) == -1) {
                // 没有实例在运行，进行拷贝
                copyFile(app.getServer(), app.getAppName(), version);
            }
        }
        app.setRunVersion(version);
        appMapper.save(app);

        // 开放端口
        CmdUtil.linuxExecShell(appAddVo.getServer(), "sudo firewall-cmd --add-port=" + app.getPort() + "/tcp --permanent");
        CmdUtil.linuxExecShell(appAddVo.getServer(), "sudo firewall-cmd --reload");
        return true;
    }

    /**
     * 删除实例配置
     */
    public AppAddVo getInitInfo(String appName) {
        List<App> apps = appMapper.selectByAppName(appName);
        AppAddVo appAddVo = new AppAddVo();
        if (CollectionUtil.isEmpty(apps)) {
            appAddVo.setJvmArgs("-Xms1g -Xmx1g -XX:+HeapDumpOnOutOfMemoryError");
            appAddVo.setAwaitStartTime("120");
            return appAddVo;
        }
        App app = apps.get(0);
        appAddVo.setJvmArgs(app.getJvmArgs());
        appAddVo.setAwaitStartTime(app.getAwaitStartTime());
        appAddVo.setShFile(app.getShFile());
        return appAddVo;
    }

    /**
     * 删除实例配置
     */
    public List<String> getServerIps() {
        return serverIps;
    }

    /**
     * 删除实例配置
     */
    public boolean delApp(Long appId) {
        App app = appMapper.selectByPrimaryKey(appId);
        if (appMapper.selectByAppName(app.getAppName()).size() == 1) {
            throw new BusinessException("当前服务只有一台实例禁止删除");
        }
        stopApp(app.getServer(), app.getPort());

        appMapper.del(appId);
        return true;
    }

    public boolean tapeOutApp(Long appId) {
        App app = appMapper.selectByPrimaryKey(appId);
        stopApp(app.getServer(), app.getPort());
        return true;
    }

    public boolean runApp(Long appId) {
        App app = appMapper.selectByPrimaryKey(appId);


        if (StringUtils.isEmpty(app.getRunVersion())) {
            throw new BusinessException("当前实例没有版本信息，请使用[立即发布]操作进行启动");
        }

        UserInfo userInfo = UserThreadLocal.get();
        String phone = userInfo.getPhone();
        LinkedHashMap<String, String> textMap = new LinkedHashMap<>();
        textMap.put("提交人：", userInfo.getUserName());
        textMap.put("项目名称：", app.getAppName());
        textMap.put("所属环境：", active);
        // 启动
        restartAppPool.execute(() -> {
            try {
                doStart(Arrays.asList(app), app.getAppName(), app.getRunVersion(), userInfo);
            } catch (Exception e) {
                log.error("项目：{}切换版本失败", app.getAppName(), e);
                sendErrorMsg(textMap, app.getAppName(), phone);
            }
        });
        return true;
    }


    /**
     * 获取目录的子目录
     *
     * @param directory
     * @return
     */
    private List<String> getFileList(String server, String directory) {
        String files = CmdUtil.linuxExecShell(server, "ls " + directory);
        if (StringUtils.isEmpty(files)) {
            log.info("getFileList, directory isnull:{}, files:{}", directory, files);
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>(Arrays.asList(files.split(" ")));
        return list;
    }


    private void updateVersion(Long id, String appName, String version, String userName) {
        Map<String, Object> param = new HashMap<>();
        param.put("idIf", id);
        param.put("appNameIf", appName);
        param.put("runVersion", version);
        param.put("updateBy", userName);

        appMapper.update(param);

    }

    private List<App> check(String appName, String publishVersion) {

        if (StringUtils.isEmpty(publishVersion)) {
            throw new BusinessException("需要切换的版本不能为空");
        }
        List<App> apps = appMapper.selectByAppName(appName);

        if (CollectionUtil.isEmpty(apps)) {
            throw new BusinessException("请先配置实例在使用");
        }
        App app = apps.get(0);
        if (publishVersion.equals(app.getRunVersion())) {
            throw new BusinessException("发布的版本和正在运行版本一致");
        }
        return apps;
    }

    private void stopApp(String server, String prot) {
        doStopApp1(server, prot);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("休眠失败");
        }
        // 双层停止
        doStopApp2(server, prot);
    }

    private void doStopApp1(String server, String prot) {
        String infoProt = CmdUtil.linuxExecShell(server, "lsof -i:" + prot);
        if (StringUtils.isEmpty(infoProt)) {
            return;
        }
        String[] list = infoProt.split("java");
        String pidStr = "";
        for (String s1 : list) {
            s1 = s1.trim();
            if (s1.endsWith("(LISTEN)")) {
                pidStr = s1;
                break;
            }
        }
        if (StringUtils.isEmpty(pidStr)) {
            log.error("stopApp: pid字符串为空：{}", infoProt);
            return;
        }
        String pId = pidStr.split(" ")[0];
        // 停止
        CmdUtil.linuxExecShell(server, "kill -9 " + pId);
    }


    private void doStopApp2(String server, String prot) {
        String infoProt = CmdUtil.linuxExecShell(server, "ps -ef|grep " + prot);
        if (StringUtils.isEmpty(infoProt) || infoProt.indexOf("java") == -1) {
            return;
        }
        String[] s = infoProt.split(" ");
        List<String> list = new ArrayList<>();
        for (String s2 : s) {
            if ("".equals(s2)) {
                continue;
            }
            list.add(s2);
        }
        // 停止
        CmdUtil.linuxExecShell(server, "kill -9 " + list.get(1));
    }


    private void doStart(List<App> apps, String appName, String publishVersion, UserInfo userInfo) {
        String userName = userInfo.getUserName();
        // 更改数据库状态
        for (App app : apps) {
            // 先暂停
            stopApp(app.getServer(), app.getPort());

            // 启动
            String shFile = app.getShFile();

            String path = rootDirectory + "/" + appName + "/" + publishVersion + "/" + shFile;


            String shell = "sh " + path + " start " + skyIp + " " + publishVersion + " " + app.getPort() + " " + app.getJvmArgs();

            log.info("publishApp执行脚本：{}", shell);

            CmdUtil.linuxExecShell(app.getServer(), shell);

            Long startTime = System.currentTimeMillis();

            Integer time = Integer.parseInt(app.getAwaitStartTime());
            Long endTime = null;
            boolean flag = false;
            do {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.error("线程休眠失败", e);
                }
                String infoProt = CmdUtil.linuxExecShell(app.getServer(), "netstat -tuln | grep " + app.getPort());
                if (!StringUtils.isEmpty(infoProt)) {
                    log.info("waitAppStart: {}, {}", infoProt, app.getPort());
                    flag = true;
                    break;
                }
                endTime = System.currentTimeMillis();
            } while (((endTime - startTime) / 1000) < time);


            if (flag) {
                // 切换成功 ，更改状态和版本
                log.info("切换成功,耗时：{}", ((System.currentTimeMillis() - startTime) / 1000));
                updateVersion(app.getId(), null, publishVersion, userName);
                continue;
            }

            if (!StringUtils.isEmpty(app.getRunVersion())) {
                shell = "sh " + path + " start " + skyIp + " " + app.getRunVersion() + " " + app.getPort() + " " + app.getJvmArgs();
                log.info("publishApp回滚脚本：{}", shell);
                CmdUtil.linuxExecShell(app.getServer(), shell);
            }
            // 切换成功 ，更改状态和版本
            updateVersion(app.getId(), null, app.getRunVersion(), userName);
            throw new BusinessException("版本发布失败");
        }

    }

}
