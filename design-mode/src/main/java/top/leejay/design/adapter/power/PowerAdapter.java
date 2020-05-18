package top.leejay.design.adapter.power;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 变压器转换电压
 */
public interface PowerAdapter {
    /**
     * 检测输入电压是否与适配器匹配
     *
     * @param ac 交流电
     * @return boolean
     */
    boolean support(Ac ac);

    /**
     * 输出交流电
     *
     * @param ac 交流电
     */
    void outPutDc(Ac ac);
}
