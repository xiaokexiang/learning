package top.leejay.jvm.gc;

/**
 * @author xiaokexiang
 */
public class Collectors {

    public static void main(String[] args) {
        int[] arr = new int[1024 * 1024 * 5];
        System.gc();
    }
}
