package top.leejay.interview.question22;

/**
 * @author xiaokexiang
 * @date 7/7/2020
 */
public class BinaryCalculate {
    public static void main(String[] args) {
        long wbit = 1L << 7;
        long rbit = wbit - 1L;
        long rfull = rbit - 1L;
        long abit = rbit | wbit;
        long sbit = ~rbit;
        long state = 1L << 8;
        int x = 0b11111111111111111111111110000000;
        int y = 0b10000000;
        System.out.println(Long.toBinaryString(wbit));
        System.out.println(wbit);
        System.out.println(Long.toBinaryString(rbit));
        System.out.println(rbit);
        System.out.println(Long.toBinaryString(rfull));
        System.out.println(rfull);
        System.out.println(Long.toBinaryString(abit));
        System.out.println(abit);
        System.out.println(Long.toBinaryString(sbit));
        System.out.println(sbit);
        System.out.println(x + y);
    }
}
