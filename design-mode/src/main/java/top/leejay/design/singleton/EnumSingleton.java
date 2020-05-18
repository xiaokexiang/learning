package top.leejay.design.singleton;

/**
 * @author Li Jie
 * @date 5/18/2020
 * 枚举单例
 * 使用枚举单例原因：
 * 1. 反射
 * 即使将类构造设置为private，仍然可以使用反射创建对象，但是反射在通过newInstance创建对象时，
 * 会检查该类是否被enum修饰，如果是则抛出异常，反射失败。
 *
 * 2. 序列化
 * 一般来说，序列化的readObject方法，不管是显式的还是默认的，它都会返回一个新建的实例，这个新建的实例不同于该类初始化时创建的实例。
 * 但是关于enum类型的序列化和反序列化做了特殊处理：序列化时将枚举类型的name属性输出，反序列化的时候通过Enum.valueOf()方法根据name查找枚举对象
 */
public enum EnumSingleton {
    /**
     * 实例
     */
    INSTANCE;
    public EnumSingleton getInstance() {
        return INSTANCE;
    }
}
