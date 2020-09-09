package top.leejay.jvm.load;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author xiaokexiang
 */
public class ClassLoaderTest {

    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = new ClassLoader() {
            // 使用loadClass会破坏双亲委派模型(历史原因导致)
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                    // 使用当前自定义类加载来加载fileName的类为二进制字节流
                    InputStream inputStream = getClass().getResourceAsStream(fileName);
                    // 如果找不到就让父类加载器去执行加载
                    if (null == inputStream) {
                        return super.loadClass(name);
                    }
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    // 创建name的Class对象
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        };
        // 通过自定义类加载器实现类的加载
        Object obj = classLoader.loadClass("top.leejay.jvm.load.ClassLoaderTest").newInstance();
        // 查看加载的Class对象
        System.out.println(obj.getClass());
        // 验证不同的类加载器加载同一个.class文件是否相同
        System.out.println(obj instanceof top.leejay.jvm.load.ClassLoaderTest);
    }
}
