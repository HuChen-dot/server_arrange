package com.server.msg;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class WeChatService {


    @Value("${webhook.url}")
    private String webhookUrl;


    /**
     * 发送消息
     *
     * @param title     标题
     * @param textMap   消息内容
     * @param atMobiles @收件人，使用手机号
     */
    public void sendToWeChat(String title, LinkedHashMap<String, String> textMap, List<String> atMobiles) {
        log.debug("WeChatService.sendToWeChat,title: {},textMap: {}, atMobiles:{}", title, textMap, atMobiles);
        Map<String, Object> requestBody = new HashMap<>(2);
        StringBuilder text = new StringBuilder();
        text.append("#### " + title).append(" \n> ");
        Set<Map.Entry<String, String>> entries = textMap.entrySet();
        int count = 1;
        for (Map.Entry<String, String> entry : entries) {
            text.append(entry.getKey()).append(entry.getValue());
            if (entries.size() != count) {
                text.append("\n\n>");
            }
            count++;
        }

        Map<String, Object> content = new HashMap<>(2);
        content.put("content", text.toString());
        requestBody.put("msgtype", "markdown");
        requestBody.put("markdown", content);
        log.debug("WeChatService.sendToWeChat,开始提交webhookUrl: {}, requestBody:{}", webhookUrl, GfJsonUtil.toJSONString(requestBody));
        HttpClient.doPost(webhookUrl, GfJsonUtil.toJSONString(requestBody));


        // 上面发完信息，在发送一条@信息
        requestBody.clear();
        content.clear();
        if (atMobiles == null || atMobiles.size() == 0) {
            atMobiles = new ArrayList<>();
            atMobiles.add("@all");
        }
        content.put("mentioned_mobile_list", atMobiles);
        content.put("content", "");
        requestBody.put("msgtype", "text");
        requestBody.put("text", content);
        HttpClient.doPost(webhookUrl, GfJsonUtil.toJSONString(requestBody));


    }
}

