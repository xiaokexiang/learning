package top.leejay.learning.leecode;

public class C3 {
    public boolean isPalindrome(int x) {
        if (x < 0) return false;// 负数肯定不是回文数
        if (x == 0) return true;
        if (x % 10 == 0) return false;// 正数但最后一位是0肯定不是回文数
        int revert = 0;// 只反转一半，只要返回后的与未反转的相等极为回文数
        while (x > revert) {
            revert = revert * 10 + x % 10;
            x /= 10;
        }
        return x == revert || x == revert / 10;
    }
}
