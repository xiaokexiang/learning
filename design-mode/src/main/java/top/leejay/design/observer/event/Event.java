package top.leejay.design.observer.event;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 定义传递的事件
 */
@Data
public class Event {
    /**
     * 事件由谁发起
     */
    private Object source;
    /**
     * 事件触发通知谁
     */
    private Object target;

    /**
     * 事件触发, 回调 做什么动作
     */
    private Method callback;
    /**
     * 事件的名称 触发的是什么事件
     */
    private String trigger;
    /**
     * 事件触发时间
     */
    private long time;

    public Event(Object target, Method callback) {
        this.target = target;
        this.callback = callback;
    }
}
