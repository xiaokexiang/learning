package top.leejay.learning.sort;

/**
 * 快速排序，时间复杂度：O(nlog^n) 最差 O(n^2)
 */
public class QuickSort {
    int sum(int[] array) {
        if (array.length < 2)
            return array[0];
        else {
            int size = array.length - 1;
            int[] newArray = new int[size];
            System.arraycopy(array, 1, newArray, 0, size);
            return array[0] + sum(newArray);
        }
    }

    public static void main(String[] args) {
        QuickSort quickSort = new QuickSort();
        int sum = quickSort.sum(new int[]{1, 2, 3, 4, 5});
        System.out.println(sum);
    }
}
