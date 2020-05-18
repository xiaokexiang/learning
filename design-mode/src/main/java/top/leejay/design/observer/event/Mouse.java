package top.leejay.design.observer.event;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 
 */
public class Mouse extends EventListener {
    public void click() {
        System.out.println("调用单击方法");
        trigger(MouseEventType.ON_CLICK);
    }

    public void doubleClick() {
        System.out.println("调用双击方法");
        trigger(MouseEventType.ON_DOUBLE_CLICK);
    }
}
