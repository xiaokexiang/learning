package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class ClassLoaderTest2 {
    public static void main(String[] args) throws ClassNotFoundException {
        MyClassLoader loader = new MyClassLoader();
        Class<?> hello = loader.loadClass("Hello");
        System.out.println(hello.getClassLoader());
    }
}
