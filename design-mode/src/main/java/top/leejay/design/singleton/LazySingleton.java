package top.leejay.design.singleton;

/**
 * @author xiaokexiang
 * @date 5/18/2020
 * 单例：懒汉式(只有当你调用的时候，发现没有才去创建) 线程不安全
 * 多线程情况下，当线程A判断 singleton = null 时准备创建对象，此时线程B获取了cpu执行权，也发现 singleton = null 也去创建对象，
 * 此时两者创建的对象不一致。
 */
public class LazySingleton {
    private static LazySingleton singleton;

    /**
     * 定义为private，这样其他类都无法通过new来创建该对象，但是无法避免反射
     */
    private LazySingleton() {}

    public static LazySingleton getInstance() {
        if (null == singleton) {
            singleton = new LazySingleton();
        }
        return singleton;
    }
}
