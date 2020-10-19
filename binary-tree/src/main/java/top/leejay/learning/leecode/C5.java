package top.leejay.learning.entity;

public class C5 {

    public static void main(String[] args) {
        String[] strs = new String[]{"flower", "flow", "flight"};
        String s = new C5().longestCommonPrefix(strs);
        System.out.println(s);
    }

    public String longestCommonPrefix(String[] strs) {
        final int len = strs.length;
        if (len == 0) return "";
        String pre = strs[0];
        for (int i = 1; i < len; i++) {
            // 只要前缀不为空就继续循环
            if((pre = prefix(pre, strs[i])).length() == 0){
                break;
            }
        }
        return pre;
    }

    public String prefix(String str1, String str2) {
        int len = Math.min(str1.length(), str2.length());// 比较长短，按照最短的循环
        int index = 0;
        while (index < len && str1.charAt(index) == str2.charAt(index)) {
            index++;
        }
        return str1.substring(0, index);
    }
}
