package top.leejay.design.adapter.demo;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 * 直流电适配器
 */
public class Dc5Adapter2 implements Dc {

    private Ac220 ac220;

    Dc5Adapter2(Ac220 ac220) {
        this.ac220 = ac220;
    }

    @Override
    public int outPutDc() {
        int ac220 = this.ac220.outPutAc();
        System.out.println("获取交流电： " + ac220);
        System.out.println("准备进行转换");
        int dc5 = ac220 / 44;
        System.out.println("转换后为： " + dc5 + "v直流电");
        return 0;
    }
}
