package top.leejay.jvm.oom;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author xiaokexiang
 */
public class Direct {
    /**
     * -XX：MaxDirectMemorySize=10m
     */
    public static void main(String[] args) throws IllegalAccessException {
        Field unsafeField = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe)unsafeField.get(null);
        while (true) {
            unsafe.allocateMemory(1024 * 1024);
        }
    }
}
