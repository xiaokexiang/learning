package top.leejay.jvm.oom;

/**
 * @author xiaokexiang
 */
public class Stack {
    public static void main(String[] args) {
        Stack stack = new Stack();
        // stackOverFlow
        stack.oom();
        // stack oom

    }

    void stackOverFlow() {
        stackOverFlow();
    }

    void oom() {
        while (true) {
            new Thread(() -> {
                while (true) {

                }
            }).start();
        }
    }
}
