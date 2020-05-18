package top.leejay.design.strategy.pay;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 支付策略类
 */
public class PayStrategy {
    private static Map<String, AbstractPayment> PAYMENT_MAP = new HashMap<>();

    static {
        PAYMENT_MAP.put(PayName.ALI_PAY, new AliPay());
        PAYMENT_MAP.put(PayName.TENCENT_PAY, new TencentPay());
        PAYMENT_MAP.put(PayName.JD_PAY, new JdPay());
    }

    public static AbstractPayment getPay(String payName) {
        if (!PAYMENT_MAP.containsKey(payName)) {
            return PAYMENT_MAP.get(PayName.DEFAULT_PAY);
        }
        return PAYMENT_MAP.get(payName);
    }

    public interface PayName {
        String DEFAULT_PAY = "AliPay";
        String ALI_PAY = "AliPay";
        String TENCENT_PAY = "TencentPay";
        String JD_PAY = "JdPay";
    }
}
