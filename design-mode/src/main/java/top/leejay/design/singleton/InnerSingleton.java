package top.leejay.design.singleton;

/**
 * @author Li Jie
 * @date 5/18/2020
 * 利用静态内部类实现单例 线程安全
 * 缺点：因为是静态内部类，对于传参不友好。
 */
public class InnerSingleton {

    private InnerSingleton() {}

    public static InnerSingleton getInstance() {
        // 内部类只有在被调用的时候才会创建，然后创建SINGLETON，实现懒加载
        return Inner.SINGLETON;
    }

    private static class Inner {
        // 对于一个类，JVM在仅用一个类加载器加载它时，静态变量的赋值在全局只会执行一次！
        // 且JVM会保证一个类的<CInit>()方法（初始化方法）执行时的线程安全，从而保证了实例在全局的唯一性
        private static InnerSingleton SINGLETON = new InnerSingleton();
    }
}
