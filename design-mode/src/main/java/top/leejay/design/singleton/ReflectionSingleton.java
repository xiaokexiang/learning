package top.leejay.design.singleton;

/**
 * @author Li Jie
 * @date 5/18/2020
 * 反射导致单例失效
 */
public class ReflectionSingleton {
    public static ReflectionSingleton singleton = new ReflectionSingleton();

    /**
     * 不允许通过反射创建
     */
    private ReflectionSingleton() {
        throw new RuntimeException("ReflectionSingleton cannot be created by reflection");
    }

    public static ReflectionSingleton getInstance() {
        return singleton;
    }
}
