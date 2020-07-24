package top.leejay.learning.sort;


import java.util.Arrays;

/**
 * 快速排序，时间复杂度：O(nlog^n) 最差 O(n^2)
 * 采用分治的原理
 */
public class QuickSort extends Sort {

    public QuickSort(int[] array) {
        super(array);
    }

    @Override
    protected void sort() {
    }


    public static void main(String[] args) {
        int[] i = new int[]{3, 4, 1, 2, 6, 8, 7, 10, 9};
        QuickSort quickSort = new QuickSort(i);
        quickSort.sort();
        System.out.println(Arrays.toString(quickSort.array));
    }
}
