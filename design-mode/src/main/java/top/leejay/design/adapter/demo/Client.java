package top.leejay.design.adapter.demo;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 */
public class Client {
    public static void main(String[] args) {
        Dc dc5Adapter1 = new Dc5Adapter1();
        dc5Adapter1.outPutDc();
        System.out.println("----------------------------------------------");
        Dc dc5Adapter2 = new Dc5Adapter2(new Ac220());
        dc5Adapter2.outPutDc();
    }
}
