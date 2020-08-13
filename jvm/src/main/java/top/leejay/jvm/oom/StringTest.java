package top.leejay.jvm.oom;

/**
 * @author xiaokexiang
 */
public class StringTest {
    public static void main(String[] args) {
        String s1 = "hello";
        System.out.println(s1 == s1.intern());// true
        String s2 = new String("hello");
        System.out.println(s2 == s2.intern());// false
    }
    // 编译阶段 hello he llo 都会进入class类常量池
    // 类加载节点 hello he llo 都会进入运行时常量池
    // resolve ldc指令 hello
}
