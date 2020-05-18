package top.leejay.design;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.leejay.design.singleton.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class DesignModeApplicationTests {

    /**
     * 饿汉式
     */
    @SneakyThrows
    @Test
    void hungrySingleton() {
        HungrySingleton one = HungrySingleton.getInstance();
        new Thread(() -> {
            HungrySingleton two = HungrySingleton.getInstance();
            System.out.println(one == two);
        }).start();
        Thread.sleep(1000L);
    }

    /**
     * 懒汉式
     */
    @SneakyThrows
    @Test
    void lazySingleton() {
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        threadPool.execute(() -> {
            for (int i = 0; i < 20; i++) {
                System.out.println(LazySingleton.getInstance());
            }
        });
    }

    /**
     * 静态内部类
     */
    @Test
    void innerSingleton() {
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        threadPool.execute(() -> {
            for (int i = 0; i < 20; i++) {
                System.out.println(InnerSingleton.getInstance());
            }
        });
    }

    /**
     * 双重确认
     */
    @Test
    void doubleCheckSingleton() {
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        threadPool.execute(() -> {
            for (int i = 0; i < 20; i++) {
                System.out.println(DoubleCheckSingleton.getInstance());
            }
        });
    }

    /**
     * 序列化导致单例失效
     */
    @Test
    void serializerSingleton() throws IOException, ClassNotFoundException {
        SerializerSingleton instance = SerializerSingleton.getInstance();
        // 序列化
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("serializerSingleton.obj"));
        outputStream.writeObject(instance);
        outputStream.flush();
        outputStream.close();

        // 反序列化
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("serializerSingleton.obj"));
        SerializerSingleton singleton = (SerializerSingleton) inputStream.readObject();
        inputStream.close();

        // false 原因在于： 任何一个readObject方法，不管是显式的还是默认的，它都会返回一个新建的实例，这个新建的实例不同于该类初始化时创建的实例
        System.out.println(singleton == instance);
    }

    /**
     * 反射导致单例失效
     */
    @SneakyThrows
    @Test
    void reflectSingleton() {
        ReflectionSingleton instance = ReflectionSingleton.getInstance();
        Constructor<ReflectionSingleton> constructor = ReflectionSingleton.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ReflectionSingleton reflectionSingleton = constructor.newInstance();
        // false
        System.out.println(instance == reflectionSingleton);
    }
}
