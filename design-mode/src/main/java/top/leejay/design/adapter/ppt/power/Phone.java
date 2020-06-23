package top.leejay.design.adapter.ppt.power;

/**
 * @author xiaokexiang
 * @since 2020/6/7
 */
public class Phone {
    public static void main(String[] args) {
        DC5 dc5 = new DC5(new AC200());
        dc5.outputDC();
    }
}
