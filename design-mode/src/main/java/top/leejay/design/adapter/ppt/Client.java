package top.leejay.design.adapter.ppt;

/**
 * @author xiaokexiang
 * @since 2020/6/7
 */
public class Client {
    public static void main(String[] args) {
        // 类适配器
        ClassAdapter adapter = new ClassAdapter();
        adapter.request();
        // 对象适配器
        ObjectAdapter objectAdapter = new ObjectAdapter(new Adaptee());
        objectAdapter.request();
    }
}
