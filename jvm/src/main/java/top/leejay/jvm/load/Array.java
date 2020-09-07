package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class Array {
    static {
        System.out.println("Array init ...");
    }
}

class Test2 {
    public static void main(String[] args) {
        Array[] a = new Array[10];
    }
}
