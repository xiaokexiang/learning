package top.leejay.learning.entity;

public class C2 {

    public int reverse(int x) {
        String binary = Integer.toString(x);
        int prefix = 1;
        if (x < 0) {
            prefix = -1;
            binary = binary.substring(1);
        }
        try {
            return Integer.parseInt(new StringBuilder(binary).reverse().toString()) * prefix;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        C2 c2 = new C2();
        System.out.println(c2.reverse(123));
    }
}
