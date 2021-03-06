package top.leejay.interview.question7;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 *
 * 1. java中使用0b表示的二进制 使用日常数字表示的就是十进制
 * 2. 无论是正数负数都在`移位操作`的时候都需转换成补码进行操作(只不过区别在于正数的补码即本身，负数的补码需要原码->反码->补码)
 * 3. 十进制移位 先计算出原码转换成补码，然后移位后将补码转换为原码 再计算十进制值
 * 4. 二进制移位 本身就是补码 直接移位 并计算十进制即可
 *
 */
public class NumberCalculate {
    public static void main(String[] args) {
        // 1000 0001(补码) -> 1000 0000 -> 1111 1111 -> -(2^6 + ... + 2^1 + 1) -> -(2*(1-2^6)/1-2 + 1) -> -127
        // 只要是符号位是负数的二进制数，在计算十进制值的时候符号位不计算在内！！！
        byte a = (byte) 0b10000001;
        System.out.println(a);

        // 需要注意是int 还是 byte
        // 0000 0000 0000 0000 0000 0000 1000 0001(原码) -> 2^7 + 2^0 -> 129
        int b = 0b10000001;
        // 0000 0000 0000 0000 0000 0010 1000 0001(原码) -> 2^9 + 2^7 + 2^0 -> 641
        int b1 = 0b1010000001;
        // 10000000000000000000000000000001(补码) -> 10000000000000000000000000000000(反码) -> 11111111111111111111111111111111
        // - (2*(1-2^30)/1-2 + 1) = -2147483647
        int b2 = 0b10000000000000000000000000000001;
        System.out.println(b);
        System.out.println(b1);
        System.out.println(b2);

        // 00000001 -> 2^0
        byte c = 0b00000001;
        System.out.println(c);

        // 转换成补码再移位 1000 0001 << 2 -> 0000 0100 -> 4
        byte d = (byte) (0b10000001 << 2);
        System.out.println(d);

        // 0000 0001 -> 0000 0000 -> 0
        byte e = 0b00000001 >> 2;
        System.out.println(e);

        // 1000 0001 -> 0010 0000 -> 32
        byte f = 0b10000001 >>> 2;
        System.out.println(f);

        // 0000 1010(原码) -> 0000 1010(补码) -> 0010 1000(移位后) -> 0010 1000(原码计算十进制值) -> 40
        byte g = 10 << 2;
        System.out.println(g);

        // 1000 1010(原码) -> 1111 0101(反码) -> 1111 0110(补码) -> 1101 1000(移位后) -> 1101 0111(反码) -> 1010 1000(原码) -> -(2^5+2^3) -> -40
        byte h = -10 << 2;
        // 1000 1010 -> 0010 1000 -> (2^5 + 2^3) -> 40
        byte i = (byte) (0b10001010 << 2);
        System.out.println(h);
        System.out.println(i);

        int j = 10 << 2;
        System.out.println(j);
    }
}
