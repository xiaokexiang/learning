package top.leejay.design.adapter.power;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 使用策略模式 & 适配器模式
 */
public class PowerAdapterStrategy {
    private static Map<String, PowerAdapter> POWER_ADAPTER = new HashMap<>();

    static {
        POWER_ADAPTER.put(AdapterName.CHINA, new ChinaDcAdapter());
        POWER_ADAPTER.put(AdapterName.JAPAN, new JapanDcAdapter());
    }

    public static PowerAdapter getPowerAdapter(String key) {
        if (!POWER_ADAPTER.containsKey(key)) {
            throw new RuntimeException("No value for this key");
        }
        return POWER_ADAPTER.get(key);
    }

    public interface AdapterName {
        String CHINA = "China";
        String JAPAN = "Japan";
    }
}

class PowerAdapterTest {
    public static void main(String[] args) {
        PowerAdapter powerAdapter = PowerAdapterStrategy.getPowerAdapter(PowerAdapterStrategy.AdapterName.CHINA);
        ChinaAc chinaAc = new ChinaAc();
        if (powerAdapter.support(chinaAc)) {
            powerAdapter.outPutDc(chinaAc);
        }
    }
}