package top.leejay.design.adapter.power;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class JapanDcAdapter implements PowerAdapter {
    @Override
    public boolean support(Ac ac) {
        return 110 == ac.output();
    }

    @Override
    public void outPutDc(Ac ac) {
        System.out.println("接受电压: " + ac.output() + "V, 准备变压。");
        int outPut = ac.output() / 22;
        System.out.println("输出电压: " + outPut + "V");
    }
}
