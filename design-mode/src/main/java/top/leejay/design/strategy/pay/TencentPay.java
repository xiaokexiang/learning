package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class TencentPay extends AbstractPayment {
    @Override
    public String getName() {
        return "微信支付";
    }

    @Override
    public double getBalance(String uid) {
        return 500;
    }
}
