package top.leejay.interview.question22;

/**
 * @author xiaokexiang
 * @since 2020/7/4
 * 模拟局部变量和成员变量指向同一个对象，局部变量修改也会导致成员变量修改
 */
public class ThreadLocalTest3 {
    static class Demo {
        int count;
    }
    private Demo share = new Demo();
    void check() {
        Demo x = share;
        x.count++;
        System.out.println("x: " + x.count + ",share: " + share.count);
    }
    public static void main(String[] args) {
        ThreadLocalTest3 test = new ThreadLocalTest3();
        test.check();
    }
}
