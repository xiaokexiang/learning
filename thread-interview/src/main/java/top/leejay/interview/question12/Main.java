package top.leejay.interview.question12;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 */
public class Main {
    public static void main(String[] args) {
        Data data = new DataImpl2(10);
        new ReadThread("read-one", data).start();
        new ReadThread("read-two", data).start();
        new ReadThread("read-three", data).start();
        new WriteThread("write-one", data, "ABCDEFGHIJKLMNOPQRSTUVWXYZ").start();
        new WriteThread("write-one", data, "abcdefghijklmnopqrstuvwxyz").start();
    }
}
