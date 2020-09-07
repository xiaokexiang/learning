package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class SuperClass {
    static {
        System.out.println("Super class init");
    }

    public static int value = 123;
}

class SubClass extends SuperClass {
    static {
        System.out.println("Sub class init");
    }
    public static int value = 456;
}

class Test {
    public static void main(String[] args) {
        System.out.println(SubClass.value);
    }
}
