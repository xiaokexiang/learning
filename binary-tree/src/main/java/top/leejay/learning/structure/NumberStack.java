package top.leejay.learning.structure;

/**
 * stack可以由数组或链表实现
 */
public class NumberStack extends NumberArrayList {
    public NumberStack(int size) {
        super(size);
    }

    /**
     * 将元素插入栈顶
     *
     * @param value value
     */
    void push(int value) {
        super.add(value, count);
    }

    /**
     * 获取栈顶元素并移除
     */
    int poll() {
        int i = super.find(count - 1);
        super.delete(count - 1);
        return i;
    }

    public static void main(String[] args) {
        NumberStack numberStack = new NumberStack(8);
        for (int i = 0; i < 6; i++) {
            numberStack.initArray(i + 2, i);
        }
        System.out.println(numberStack);
        numberStack.push(99);
        numberStack.push(66);
        System.out.println(numberStack);
        numberStack.poll();
        System.out.println(numberStack);
        numberStack.poll();
        System.out.println(numberStack);
    }
}
