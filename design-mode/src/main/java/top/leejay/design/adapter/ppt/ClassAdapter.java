package top.leejay.design.adapter.ppt;

/**
 * @author xiaokexiang
 * @since 2020/6/7
 * 适配器类
 */
public class ClassAdapter extends Adaptee implements Target {

    @Override
    public void request() {
        super.adapteeRequest();
    }
}
