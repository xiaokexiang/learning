package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class Test1 {
    public static void main(String[] args) {
        System.out.println(son.B);
    }
}

class Parent {
    static int A = 0;
    static {
        A = 2;
    }
}

class son extends Parent {
    public static int B = A;
}
