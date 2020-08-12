package top.leejay.jvm.oom;

/**
 * @author xiaokexiang
 */
public class StringTest {
    public static void main(String[] args) {
        String s4 = "hello";
        String s5 = s4.intern();
        String s6 = new String("12");
        String s7 = s6.intern();
        System.out.println(s4 == s5);
        System.out.println(s6 == s7);
    }
    // 编译阶段 hello he llo 都会进入class类常量池
    // 类加载节点 hello he llo 都会进入运行时常量池
    // resolve ldc指令 hello
}
