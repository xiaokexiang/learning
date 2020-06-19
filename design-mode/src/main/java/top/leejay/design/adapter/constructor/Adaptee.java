package top.leejay.design.adapter.constructor;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 * 被适配者类
 */
public class Adaptee {
    /**
     * 被适配者提供方法，需要被转换
     */
    void adapted(String name) {
        System.out.println("adaptee was called from " + name);
    }
}
