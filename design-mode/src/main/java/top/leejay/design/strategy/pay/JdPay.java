package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class JdPay extends AbstractPayment {
    @Override
    public String getName() {
        return "京东白条";
    }

    @Override
    public double getBalance(String uid) {
        return 2000;
    }
}
