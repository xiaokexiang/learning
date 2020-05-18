package top.leejay.design.prototype;

import java.io.Serializable;

/**
 * @author xiaokexiang
 * @date 11/5/2019
 * 
 */
public class JinGuBang implements Serializable {
    public float h =100;
    public float d = 10;

    public void big() {
        this.d *= 2;
        this.h *= 2;
    }

    public void small() {
        this.d /= 2;
        this.h /= 2;
    }
}
