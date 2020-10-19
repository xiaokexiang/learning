package top.leejay.learning.leecode;

import java.util.HashMap;
import java.util.Map;

public class C4 {
    public static void main(String[] args) {
        String s = "IIV";
        System.out.println(check(s));
    }
    private static int check(String s) {
        Map<Character, Integer> map = new HashMap<>();
        map.put('I', 1);
        map.put('V', 5);
        map.put('X', 10);
        map.put('L', 50);
        map.put('C', 100);
        map.put('D', 500);
        map.put('M', 1000);
        int sum = 0;
        Integer preNum = map.get(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            Integer num = map.get(s.charAt(i));
            if (preNum < num) {
                sum -= preNum;
            } else {
                sum += preNum;
            }
            preNum = num;
        }
        sum += preNum;
        return sum;
    }
}
