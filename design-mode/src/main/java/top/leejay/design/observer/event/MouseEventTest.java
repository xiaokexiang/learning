package top.leejay.design.observer.event;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 测试类
 */
public class MouseEventTest {
    public static void main(String[] args) {

        /*Mouse类 就是监听器具体体现*/
        Mouse mouse = new Mouse();
        MouseEventCallback callback = new MouseEventCallback();
        mouse.addListener(MouseEventType.ON_CLICK, callback);
        mouse.addListener(MouseEventType.ON_DOUBLE_CLICK, callback);
        mouse.click();
        mouse.doubleClick();
    }
}
