package top.leejay.design.observer.guava;

import com.google.common.eventbus.EventBus;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 
 */
public class GuavaEventTest {
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        GuavaEvent guavaEvent = new GuavaEvent();
        // 注册观察者
        eventBus.register(guavaEvent);
        // 传播变化
        eventBus.post("hello world");
    }
}
