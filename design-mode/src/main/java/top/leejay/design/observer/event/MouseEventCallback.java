package top.leejay.design.observer.event;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 
 */
public class MouseEventCallback {

    public void onClick(Event event) {
        System.out.println("================触发鼠标单击回调事件================" + "\n" + event);
    }

    public void onDoubleClick(Event event) {
        System.out.println("================触发鼠标双击回调事件================" + "\n" + event);
    }
}
