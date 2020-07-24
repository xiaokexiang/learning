package top.leejay.learning.sort;


import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 冒泡排序: O(n^2)
 */
@Slf4j
public class BubbleSort extends Sort {
    public BubbleSort(int[] array) {
        super(array);
    }

    @Override
    public void sort() {
        long start = System.nanoTime();
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1; j++) {
                if (array[j] < array[j + 1]) {
                    int temp = array[j + 1];
                    array[j + 1] = array[j];
                    array[j] = temp;
                }
            }
        }
        log.info("time: {}", System.nanoTime() - start);
    }

    /**
     * 冒泡排序的优化：在循环中如果没有元素移位，说明此时已经是正确的顺序，直接退出循环
     * [3, 1, 2, 4, 5, 6, 7, 8, 9, 10]
     */
    private void sortAdvance() {
        long start = System.nanoTime();
        for (int i = 0; i < array.length - 1; i++) {
            boolean isBreak = true;
            for (int j = 0; j < array.length - 1; j++) {
                if (array[j] < array[j + 1]) {
                    int temp = array[j + 1];
                    array[j + 1] = array[j];
                    array[j] = temp;
                    isBreak = false;
                }
            }
            // 说明此时没有元素移位
            if (isBreak) {
                break;
            }
        }
        log.info("time: {}", System.nanoTime() - start);
    }

    /**
     * 冒泡排序优化2:针对数组中某些部分已经是符合顺序的数据，修改循环index，只循环不符合顺序的部分
     * [3, 4, 1, 5, 2, 6, 7, 8, 9, 10]
     */
    private void sortAdvance2() {
        long start = System.nanoTime();
        int border = 0;
        int limit = array.length - 1;
        for (int i = 0; i < array.length - 1; i++) {
            boolean isBreak = true;
            for (int j = 0; j < limit; j++) {
                if (array[j] < array[j + 1]) {
                    int temp = array[j + 1];
                    array[j + 1] = array[j];
                    array[j] = temp;
                    isBreak = false;
                    // 只要有换位，border就会改变
                    border = j;
                }
            }
            // 修改边界，下一次循环只需要到limit就行
            limit = border;
            // 说明此时没有元素移位
            if (isBreak) {
                break;
            }
        }
        log.info("time: {}", System.nanoTime() - start);
    }

    public static void main(String[] args) {
        int[] i = new int[]{3, 4, 1, 2, 6, 8, 7, 10, 9, 5};
        BubbleSort bubbleSort = new BubbleSort(i);
        bubbleSort.sortAdvance2();
        System.out.println(Arrays.toString(bubbleSort.getArray()));

//        bubbleSort.sortAdvance();
//        System.out.println(Arrays.toString(bubbleSort.getArray()));


    }
}
