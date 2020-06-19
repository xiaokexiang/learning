package top.leejay.design.adapter.constructor;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 */
public class Client {
    public static void main(String[] args) {
        Target adapterClass = new AdapterClass();
        adapterClass.request();

        Target adapterObject = new AdapterObject(new Adaptee());
        adapterObject.request();
    }
}
