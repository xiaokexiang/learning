package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class ClassReflect {

    static int value = 0;

    static {
        System.out.println("static ...");
        value = 1;
        System.out.println(value);
    }
}
