package top.leejay.learning.leecode;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class C6 {

    public static void main(String[] args) {
        C6 c6 = new C6();
        System.out.println(c6.isValid("([]){}"));
    }

    public boolean isValid(String s) {
        Map<Character, Character> map = new HashMap<>();
        map.put(')', '(');
        map.put(']', '[');
        map.put('}', '{');

        final int len = s.length();
        if (len == 0 || len % 2 != 0) return false;// 奇数肯定invalid

        Deque<Character> stack = new LinkedList<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (map.containsKey(ch)) {
                if (stack.isEmpty() || stack.peek() != map.get(ch)) {
                    return false;
                }
                stack.pop();
            } else {
                stack.push(ch);
            }
        }
        return stack.isEmpty();
    }
}
