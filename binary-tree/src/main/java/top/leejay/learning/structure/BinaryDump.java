package top.leejay.learning.structure;

import java.util.Arrays;

/**
 * 二叉堆
 */
public class BinaryDump {

    private boolean fast;
    private int[] array;

    public BinaryDump(int[] array, boolean fast) {
        this.fast = fast;
        if (fast) {
            buildFast(array);
        } else {
            build(array);
        }
    }

    /**
     * 根据index找到它的parent index
     *
     * @param c childIndex
     * @return parentIndex
     */
    int prev(int c) {
        if (c <= 0) {
            return -1;
        }
        return c % 2 != 0 ? (c - 1) >>> 1 : (c - 2) >>> 1;
    }

    /**
     * 将数组构建成成最小二叉堆
     * 逻辑： 从堆底元素开始，一个个堆顶遍历然后依次和节点的父节点比较，该方法会将所有的元素重新比较一次
     * 时间复杂度: O(nlogn)
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
     * 将数组构建成成最小二叉堆，该算法事件复杂度：O(n)
     * 我们可以知道同一层的节点的最多交换次数是相同的，最后一层的叶子节点不需要执行交换，因为它的下面没有节点了。
     * 所以计算 sum(交换次数) = 每层节点数 * 该层节点的最多交换次数。时间复杂度即为 O(sum(交换次数))
     * 公式推导：
     * 我们设数组大小为n，二叉堆层数为k,可知k = log2n
     * 总结规律，第一层有1个节点，第二层有2个节点，第三层有4个节点...第K层有2^(k-1)个节点
     * 第一层节点需要交换k-1次，第二层节点需要交换k-2次...第K层需要交换0次(叶子节点不需要交换)
     * 得出公式：
     * sum(k) = 1 * (k-1) + 2 * (k-2) + ...+ 2^(k-3) * 2 + 2^(k-2) * 1 + 2^(k-1) * 0
     * ->   sum(k) = 1 * (k-1) + 2 * (k-2) + ...+ 2^(k-3) * 2 + 2^(k-2) * 1
     * -> ① sum(k) = 2^0 * (k-1) + 2^1 * (k-2) +...+ 2^(k-3) * 2 + 2^(k-2) * 1
     * -> ② 2sum(k) = 2^1 * (k-1) + 2^2 * (k-2) + ... + 2^(k-2) * 2 + 2^(k-1) * 1
     * 错位相减，将2sum(k)中的(k-1)与sum(k)中的(k-1)相减，以此类推
     * -> sum(k) = ② - ① = -2^0 * (k-1) + 2^1 + 2^2 + ... + 2^(k-2) * 1 + 2^(k-1) * 1
     * -> sum(k) = -k + 1 + 2 + 4 + ... + 2^(k-1)
     * 基于等比数列求和公式
     * -> sum(k) = -k + 1 + ((2 - 2 * 2^(k-1)) / -1)
     * -> sum(k) = 2^k - k - 1
     * 基于 k = log2n
     * -> sum(n) = n - log2n - 1
     * 所以此算法复杂度为 O(n)
     *
     * @param array array
     */
    public void buildFast(int[] array) {
        int size;
        // 从非叶子节点开始遍历，从后往前遍历
        for (int i = (size = array.length >>> 1) - 1; i >= 0; i--) {
            int k = i;
            // 遍历查找当前index的左右节点是否都小于当前节点
            // 退出条件：右节点index超过数组大小
            while (k < size) {
                int le = (k << 1) + 1;
                int rt = le + 1;
                int min = Math.min(array[le], array[rt]);
                // 如果当前节点大于子节点中的最小节点，那么需要移位
                if (array[k] > min) {
                    int temp = array[k];
                    array[k] = min;
                    if (array[le] > array[rt]) {
                        array[rt] = temp;
                    } else {
                        array[le] = temp;
                    }
                }
                // 继续判断直到左右节点溢出
                k = rt;
            }
        }
        this.array = array;
    }


    /**
     * 添加元素到二叉堆中，默认添加到队尾
     * 需要对数组进行扩容
     */
    int[] add(int i) {
        int size = array.length;
        int[] arr = new int[size + 1];
        System.arraycopy(array, 0, arr, 0, size);
        arr[size] = i;
        return fast ? siftUp(arr) : build(arr);
    }

    /**
     * 移除队首元素，将队尾元素移到队首，再重新构建
     */
    int delete() {
        int size = array.length;
        int first = array[0];
        int[] arr = new int[size - 1];
        // 将队尾移到队首
        array[0] = array[size - 1];
        System.arraycopy(array, 0, arr, 0, size - 1);
        this.array = fast ? siftDown(arr) : build(arr);
        return first;
    }

    /**
     * 从堆底往上替换
     * 时间复杂度： O(logn)
     *
     * @param array 默认新增元素在队尾
     * @return 添加后的最小二叉堆
     */
    private int[] siftUp(int[] array) {
        int last = array.length - 1;
        int prev;
        while ((prev = prev(last)) > 0) {
            if (array[last] < array[prev]) {
                int temp = array[prev];
                array[prev] = array[last];
                array[last] = temp;
            }
            last = prev;
        }
        this.array = array;
        return array;
    }

    /**
     * 从堆顶往堆底遍历，依次和左右子节点中较小的交换
     * 同样的从上往下替换，最多处理O(树高)次 -> O(logn)
     * 时间复杂度： O(logn)
     *
     * @param array array
     * @return 删除后的最小二叉堆
     */
    private int[] siftDown(int[] array) {
        int k = 0;
        int size = array.length;
        int le, rt;
        // 只要当前节点的左子节点小于size就继续循环
        while ((le = (k << 1) + 1) < size) {
            // 存在左节点没越界，但右节点越界情况
            // 此时当前节点只有一个左节点，需要当前节点和左节点比较
            if ((rt = le + 1) >= size && array[k] > array[le]) {
                int temp = array[le];
                array[le] = array[k];
                array[k] = temp;
                k = le;
            } else {
                // 此时说明左右节点都存在
                int min = Math.min(array[le], array[rt]);
                int temp = array[k];
                array[k] = min;
                // 注意这里的index要和左右子节点较小的交换
                if (array[le] > array[rt]) {
                    array[rt] = temp;
                    k = rt;
                } else {
                    array[le] = temp;
                    k = le;
                }
            }
        }
        return array;
    }


    public static void main(String[] args) {
        int[] i = new int[]{3, 5, 2, 4, 9, 10, 7};
        BinaryDump binaryDump = new BinaryDump(i, false);
        System.out.println(Arrays.toString(binaryDump.array));
        System.out.println(Arrays.toString(binaryDump.add(6)));
        System.out.println(Arrays.toString(binaryDump.add(11)));
        System.out.println(Arrays.toString(binaryDump.add(8)));
        System.out.println(binaryDump.delete());
        System.out.println(Arrays.toString(binaryDump.array));
        System.out.println(binaryDump.delete());
        System.out.println(Arrays.toString(binaryDump.array));
        System.out.println(binaryDump.delete());
        System.out.println(Arrays.toString(binaryDump.array));
    }
}
