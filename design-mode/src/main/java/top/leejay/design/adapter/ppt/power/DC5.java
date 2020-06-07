package top.leejay.design.adapter.ppt.power;

/**
 * @author xiaokexiang
 * @since 2020/6/7
 */
public class DC5 implements DC {

    private AC200 ac200;

    public DC5(AC200 ac200) {
        this.ac200 = ac200;
    }

    @Override
    public int outputDC() {
        // 获取交流电
        int ac = ac200.outputAC();
        // 变压器，将AC200转成DC5
        int dc = ac / 44;
        System.out.println("输出: " + dc + "V");
        return dc;
    }
}
