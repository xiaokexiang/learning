package top.leejay.design.singleton;

import java.io.Serializable;

/**
 * @author xiaokexiang
 * @date 5/18/2020
 */
public class SerializerSingleton implements Serializable {

    private static SerializerSingleton singleton = new SerializerSingleton();

    private SerializerSingleton() {
    }

    public static SerializerSingleton getInstance() {
        return singleton;
    }

    /**
     * 添加该方法能保证反序列化readObject()时获取的仍是唯一实例
     */
    private Object readResolve() {
        return singleton;
    }
}
