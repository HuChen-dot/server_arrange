package com.server.util;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;


public class CmdUtil {


    private static String ifconfig = null;

    /**
     * linux 运行shell脚本
     *
     * @param shell 需要运行的shell脚本路径:示例：/software/hello.sh
     */
    public static String linuxExecShell(String shell) {
        try {
            shell = shell.replace("*", "|");
            String[] cmds = {"/bin/sh", "-c", shell};
            Process pro = Runtime.getRuntime().exec(cmds);
            return getExecResult(pro);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * linux 运行shell脚本
     *
     * @param shell 需要运行的shell脚本路径:示例：/software/hello.sh
     */
    public static String linuxExecShell(String ip, String shell) {
        try {
            if(StringUtils.isEmpty(ip)){
                // 代表在本机器运行
                return linuxExecShell(shell);
            }
            if(StringUtils.isEmpty(ifconfig)) {
                ifconfig = linuxExecShell("ifconfig");
            }
            if (ifconfig.indexOf(ip) != -1) {
                // 代表在本机器运行
                return linuxExecShell(shell);
            }

            // 前往目标服务器运行
            return linuxExecShell("ssh root@" + ip + " " + shell);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * windows cmd执行多条命令
     */
    public static String windowExecuteCmd(List<String> cmds) {
        try {
            String cmdBin = "cmd.exe /C start /b ";
            String cmdStr = cmds.stream().collect(Collectors.joining("&&"));
            ;
            Process process = Runtime.getRuntime().exec(cmdBin + cmdStr);
            String result = getExecResult(process);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * windows 执行单条命令
     *
     * @param cmd
     * @return
     */
    public static String windowExecuteCmd(String cmd) {
        try {
            String cmdBin = "cmd.exe /C start /b ";
            Process process = Runtime.getRuntime().exec(cmdBin + cmd);
            String result = getExecResult(process);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static String getExecResult(Process process) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(getsystemLanguage())));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line).append(" ");
        }
        if (result.length() <= 0) {
            return null;
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }


    /**
     * 获取操作系统默认语言
     */
    private static String getsystemLanguage() {
        return null == System.getProperty("sun.jnu.encoding") ? "GBK"
                : System.getProperty("sun.jnu.encoding");
    }

}

