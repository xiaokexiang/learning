package com.siemens.websocket.basicone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

/**
 * @author xiaokexiang
 * @since 2020/3/4
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket1")
                .setAllowedOrigins("*");
//                .addInterceptors() 自定义拦截器
//                .setHandshakeHandler() 用户认证信息处理
//                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        return false;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 订阅broker名称
        registry.enableSimpleBroker("/topic1");
        // 全局使用的消息前缀
        registry.setApplicationDestinationPrefixes("/publish1");
    }
}
