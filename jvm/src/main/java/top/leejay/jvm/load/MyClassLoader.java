package top.leejay.jvm.load;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author xiaokexiang
 */
public class MyClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) {
        Class<?> c = findLoadedClass(name);
        if (null == c) {
            try {
                FileInputStream inputStream = new FileInputStream(new File("D://" + name + ".class"));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[inputStream.available()];
                int index;
                while ((index = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, index);
                }
                byte[] byteArray = outputStream.toByteArray();
                // 创建name的Class对象
                c = defineClass(name, byteArray, 0, byteArray.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return c;
    }
}
