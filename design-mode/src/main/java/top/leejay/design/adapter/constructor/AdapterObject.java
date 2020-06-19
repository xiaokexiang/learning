package top.leejay.design.adapter.constructor;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 * 对象适配器
 */
public class AdapterObject implements Target {

    private Adaptee adaptee;

    AdapterObject(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void request() {
        adaptee.adapted("object adapter");
    }
}
