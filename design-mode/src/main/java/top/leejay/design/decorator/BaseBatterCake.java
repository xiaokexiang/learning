package top.leejay.design.decorator;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class BaseBatterCake extends AbstractBatterCake {
    @Override
    protected String getMsg() {
        return "煎饼";
    }

    @Override
    protected int getPrice() {
        return 5;
    }
}
