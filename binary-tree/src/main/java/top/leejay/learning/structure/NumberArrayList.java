package top.leejay.learning.structure;

import java.util.Arrays;

/**
 * int数组的初始化、插入、删除和扩容。
 */
public class NumberArrayList {
    protected int[] array;
    /**
     * 表示当前array中的元素个数
     */
    protected int count;

    public NumberArrayList(int size) {
        this.array = new int[size];
    }

    protected void initArray(int value, int index) {
        if (index < 0 || index >= array.length || value == 0) {
            throw new IllegalArgumentException("parameters invalid");
        }
        array[index] = value;
        count++;
    }

    /**
     * 通过索引查找
     * O(1)
     * 因为数组在内存中是连续的，所以直到了index=0的内存地址，那么index=5的内存地址也可以直到
     *
     * @param index index
     * @return array[index]
     */
    int find(int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException("index over limit");
        }
        return array[index];
    }

    /**
     * 指定index插入元素，此index后元素向右移动
     * O(n)
     *
     * @param value value
     * @param index index
     */
    void add(int value, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index over limit");
        }
        if (count >= array.length) {
            resize();
        }
        System.arraycopy(array, index, array, index + 1, count - index);
        array[index] = value;
        count++;
    }

    /**
     * 扩容两倍
     * O(n)
     */
    void resize() {
        int[] newArray = new int[count * 2];
        System.arraycopy(array, 0, newArray, 0, count);
        array = newArray;
    }

    /**
     * 删除已存在的元素，并将该元素的👉往👈移动
     * O(n)
     *
     * @param index index
     */
    void delete(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("index over limit");
        }
        if (index != count - 1) {
            System.arraycopy(array, index + 1, array, index, count - index);
        }
        array[count - 1] = 0;
        count--;
    }

    /**
     * 删除元素，直接将最后一个元素移动到删除的index位置，此操作会导致无序
     * O(1)
     */
    void deleteNoSorted(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("index over limit");
        }
        array[index] = array[count - 1];
        array[count - 1] = 0;
        count--;
    }

    @Override
    public String toString() {
        return "ArrayTest{" +
                "array=" + Arrays.toString(array) +
                ", count=" + count +
                '}';
    }

    public static void main(String[] args) {
        NumberArrayList arrayTest = new NumberArrayList(8);
        for (int i = 0; i < 6; i++) {
            arrayTest.initArray(i + 2, i);
        }
        arrayTest.add(18, 4);
        System.out.println(arrayTest.toString());
        arrayTest.add(2, 7);
        System.out.println(arrayTest.toString());
        arrayTest.add(33, 8);
        System.out.println(arrayTest.toString());
        arrayTest.delete(2);
        System.out.println(arrayTest.toString());
        arrayTest.delete(7);
        System.out.println(arrayTest.toString());
        arrayTest.deleteNoSorted(6);
        System.out.println(arrayTest.toString());
        arrayTest.deleteNoSorted(3);
        System.out.println(arrayTest.toString());
    }
}
