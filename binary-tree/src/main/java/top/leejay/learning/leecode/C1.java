package top.leejay.learning.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class C1 {

    public int[] twoSum(int[] nums, int target) {
        if (nums.length == 0) return new int[0];
        Map<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < nums.length; i++) {
            if (map.containsKey(target - nums[i])){
                return new int[]{map.get(target - nums[i]), i};
            }
            map.put(nums[i], i);
        }
        return new int[0];
    }

    public static void main(String[] args) {

        C1 some = new C1();
        int[] ints = some.twoSum(new int[]{2, 5, 7, 11, 13}, 9);
        System.out.println(Arrays.toString(ints));
    }
}
