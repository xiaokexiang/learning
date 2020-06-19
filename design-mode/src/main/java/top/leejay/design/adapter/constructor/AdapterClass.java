package top.leejay.design.adapter.constructor;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 * 类适配器
 */
public class AdapterClass extends Adaptee implements Target  {

    @Override
    public void request() {
        super.adapted("class adapter");
    }
}
