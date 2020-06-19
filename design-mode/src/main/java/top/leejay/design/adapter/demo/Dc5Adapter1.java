package top.leejay.design.adapter.demo;

/**
 * @author xiaokexiang
 * @date 6/19/2020
 * 直流电适配器
 */
public class Dc5Adapter1 extends Ac220 implements Dc {

    @Override
    public int outPutDc() {
        // 获取交流电
        int ac220 = super.outPutAc();
        System.out.println("获取交流电： " + ac220);
        // 转换为直流电
        System.out.println("准备进行转换");
        int dc5 = ac220 / 44;
        System.out.println("转换后为： " + dc5 + "v直流电");
        return dc5;
    }
}
