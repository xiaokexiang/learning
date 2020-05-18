package top.leejay.design.adapter.power;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 模拟中国220V交流电
 */
public class ChinaAc implements Ac {

    @Override
    public int output() {
        return 220;
    }
}
