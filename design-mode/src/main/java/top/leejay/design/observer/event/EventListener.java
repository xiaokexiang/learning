package top.leejay.design.observer.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 监听器 实现通知监听者的关键
 */
public class EventListener {
    /**
     * JDK底层的Listener通常也是这样设计的
     */
    protected Map<String, Event> events = new HashMap<>();

    /**
     * 添加监听者
     *
     * @param eventType 事件类型
     * @param target    监听者
     */
    public void addListener(String eventType, Object target) {
        try {
            /*method name 要和 MouseEventCallback类中的method 对应*/
            this.addListener(eventType, target, target.getClass().getMethod("on" + toUpperFirstCase(eventType), Event.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void addListener(String eventType, Object target, Method callback) {
        events.put(eventType, new Event(target, callback));
    }

    /**
     * 通过事件动作名 触发事件
     *
     * @param trigger 动作名
     */
    protected void trigger(String trigger) {
        if (this.events.containsKey(trigger)) {
            trigger(events.get(trigger));
        }
    }

    /**
     * 事件通过名称触发
     *
     * @param event 事件
     */
    private void trigger(Event event) {
        event.setSource(this);
        event.setTime(System.currentTimeMillis());
        if (null != event.getCallback()) {
            try {
                event.getCallback().invoke(event.getTarget(), event);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private String toUpperFirstCase(String eventType) {
        char[] array = eventType.toCharArray();
        // 转换成大写
        array[0] -= 32;
        return String.valueOf(array);
    }


}
