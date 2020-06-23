package top.leejay.design.adapter.ppt;

/**
 * @author xiaokexiang
 * @since 2020/6/7
 */
public class ObjectAdapter implements Target {
    private Adaptee adaptee;

    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void request() {
        adaptee.adapteeRequest();
    }
}
