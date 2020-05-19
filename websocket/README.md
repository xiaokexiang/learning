### 简介
`基于stomp协议的Springboot-Websocket示例，其中包含spring-gateway网关转发以及使用stompJS的 Web client请求页面`

### 流程图
![流程示意图](https://image.leejay.top/image/20200519/xpjwwqc1Cnx9.png?imageslim)


### 为什么选择gateway?

`gateway作为网关，相比zuul，支持长连接，对于websocket有着更好的支持。`

### 转发配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ofm-basic1
          uri: lb://ofm-basic1
          predicates:
            - Path=/index
        # 转发到basic-one
        - id: websocket1
          uri: lb:ws://ofm-basic1
          predicates:
            - Path=/websocket1/**
        # 转发到basic-two
        - id: websocket2
            uri: lb:ws://ofm-basic2
            predicates:
            - Path=/websocket2/**
```

### server端配置
`需要注意的是不同的springboot版本，实现的接口不同，我采用的是2.2.7RELEASE版本。`

#### 注册配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket1")
                .setAllowedOrigins("*");
//                .addInterceptors() 自定义拦截器
//                .setHandshakeHandler() 用户认证信息处理
//                .withSockJS(); client使用的是socketJS
    }
   ...

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 订阅broker名称,用于接收client的订阅
        registry.enableSimpleBroker("/topic1");
        // 全局使用的消息推送前缀
        registry.setApplicationDestinationPrefixes("/publish1");
    }
}
```
#### 发送与接收

```java
@Controller
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate template;

    /**
     * 定时发送信息到前端
     */
    @Scheduled(cron = "0/10 * * * * ? ")
    public void say() {
        template.convertAndSend("/topic1/basic1", "hello! i'm basic-one");
    }
    
    /**
     * 接收前端发送的信息
     */
    @MessageMapping("/basic1")
    public void receive(JSONObject value) {
        System.out.println("message from client: " + value.get("message"));
    }

}
```

### client 配置

<a hrefp="./index.html">Client.html</a>