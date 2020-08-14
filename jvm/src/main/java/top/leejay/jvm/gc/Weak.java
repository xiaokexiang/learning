package top.leejay.jvm.gc;


import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * @author xiaokexiang
 */
public class Weak {
    private static WeakReference<String> weakReference;

    public static void main(String[] args) {
        test();
        System.out.println(weakReference.get());// hello
        System.gc(); // test作用域结束，gc会清理weakReference
        System.out.println(weakReference.get());// null
    }

    static void test() {
        // str 作为test方法的本地变量
        String str = new String("hello");
        weakReference = new WeakReference<>(str);
        System.gc();// 不会被清理
        System.out.println(weakReference.get());// hello
    }
}
