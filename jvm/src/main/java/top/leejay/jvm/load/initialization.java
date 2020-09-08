package top.leejay.jvm.load;

/**
 * @author xiaokexiang
 */
public class initialization {
    static {
        i = 1;
//        System.out.println(i);
    }
    static int i = 0;

    public static void main(String[] args) {
        System.out.println(i);
    }
}
