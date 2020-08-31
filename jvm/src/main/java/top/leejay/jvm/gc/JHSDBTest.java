package top.leejay.jvm.gc;

/**
 * @author xiaokexiang
 */
public class JHSDBTest {
    static ObjectHolder staticObject = new ObjectHolder();
    ObjectHolder objectHolder = new ObjectHolder();

    void foo() {
        ObjectHolder localObject = new ObjectHolder();
        System.out.println("done ...");
    }

    public static void main(String[] args) {
        JHSDBTest jhsdbTest = new JHSDBTest();
        jhsdbTest.foo();
    }

    private static class ObjectHolder {
    }
}
