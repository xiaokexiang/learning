package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class ClassTest {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> classReflect = Class.forName("top.leejay.jvm.load.ClassReflect");
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass("top.leejay.jvm.load.ClassReflect");
        System.out.println(aClass);
    }
}
