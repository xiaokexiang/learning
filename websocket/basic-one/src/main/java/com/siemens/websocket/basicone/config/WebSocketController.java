package com.siemens.websocket.basicone.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

/**
 * @author xiaokexiang
 * @since 2020/3/4
 */

@Controller
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate template;

    @Scheduled(cron = "0/10 * * * * ? ")
    public void say() {
        template.convertAndSend("/topic1/basic1", "hello! i'm basic-one");
    }

    @MessageMapping("/basic1")
    public void receive(JSONObject value) {
        System.out.println("message from client: " + value.get("message"));
    }

}
