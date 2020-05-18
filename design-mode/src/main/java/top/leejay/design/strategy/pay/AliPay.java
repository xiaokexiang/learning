package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class AliPay extends AbstractPayment {
    @Override
    public String getName() {
        return "支付宝";
    }

    @Override
    public double getBalance(String uid) {
        return 900.00;
    }
}
