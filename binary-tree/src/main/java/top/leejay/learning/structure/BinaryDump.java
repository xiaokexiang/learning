package top.leejay.learning.structure;

import java.util.Arrays;

/**
 * 二叉堆
 */
public class BinaryDump {

    private int[] array;

    public BinaryDump(int[] array) {
        build(array);
    }

    /**
     * 根据index找到它的parent index
     *
     * @param c childIndex
     * @return parentIndex
     */
    int prev(int c) {
        return c % 2 != 0 ? (c - 1) / 2 : (c - 2) / 2;
    }

    /**
     * 将数组构建成成最小二叉堆
     * 该算法的时间复杂度为O(nlogn),但二叉堆的最优是O(n)
     *
     * @return 构建后的最小二叉堆
     */
    private int[] build(int[] array) {
        int last = array.length - 1;
        // 从队尾开始往前比较,队首不需要比较
        for (int i = last; i > 0; i--) {
            int c = i;
            int p = prev(c);
            // 如果当前节点小于父节点，那么继续循环比较
            while (array[c] < array[p]) {
                // 交换位置和index
                int temp = array[p];
                array[p] = array[c];
                array[c] = temp;
                c = p;
                p = prev(c);
                // 跳出循环的条件:parentIndex<0
                if (p < 0) {
                    break;
                }
            }
        }
        this.array = array;
        return array;
    }


    /**
     * 计算树的深度
     *
     * @return depth
     */
    int calculateDepth() {
        if (array.length == 0) {
            return 0;
        }
        return Integer.parseInt(
                Double.toString(Math.log(array.length + 1) / Math.log(2)).split("\\.")[0]);
    }

    /**
     * 添加元素到二叉堆中
     */
    int[] add(int i) {
        int size = array.length;
        int[] arr = new int[size + 1];
        System.arraycopy(array, 0, arr, 0, size);
        arr[size] = i;
        return build(arr);
    }

    /**
     * 将数组中指定index的元素移除
     * 将最后一个元素移到被删除index位置，再重新build
     */
    int[] delete(int index) {
        int size = array.length;
        int[] arr = new int[size - 1];
        if (index == size - 1) {
            System.arraycopy(array, 0, arr, 0, size - 1);
            return arr;
        }
        int p = index;
        array[p] = array[size - 1];
        System.arraycopy(array, 0, arr, 0, size - 1);
        return build(arr);
    }

    public static void main(String[] args) {
        int[] i = new int[]{3, 5, 2, 4, 9, 10, 7};
        BinaryDump binaryDump = new BinaryDump(i);
        System.out.println(Arrays.toString(binaryDump.add(6)));
        System.out.println(Arrays.toString(binaryDump.delete(3)));
    }

}
