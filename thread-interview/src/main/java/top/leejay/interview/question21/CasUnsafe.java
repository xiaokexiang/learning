package top.leejay.interview.question21;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author xiaokexiang
 * @date 6/8/2020
 * 通过Unsafe类操作cas api 模拟AtomicInteger
 */
public class CasUnsafe {
    private static Unsafe unsafe;
    private volatile int value;
    private static long valueOffSet;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            // 字段不是静态字段的话,要传入反射类的对象;如果字段是静态字段的话,传入任何对象都是可以的,包括null.
            unsafe = (Unsafe) field.get(null);
            valueOffSet = unsafe.objectFieldOffset(CasUnsafe.class.getDeclaredField("value"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public CasUnsafe(int value) {
        this.value = value;
    }

    public int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffSet, 1) + 1;
    }

    public int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffSet, -1) + 1;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
