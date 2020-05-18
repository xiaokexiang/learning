package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 支付状态
 */
public class PayState<T> {
    private int code;
    private String message;
    private T data;

    public PayState(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return ("支付状态: [" + code + "], " + message + ",交易详情: " + data);
    }
}
