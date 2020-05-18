package top.leejay.design.prototype;

/**
 * @author xiaokexiang
 * @date 11/5/2019
 * 
 */
public class Client {
    private Prototype prototype;

    public Client(Prototype prototype) {
        this.prototype = prototype;
    }

    public Prototype startClone(Prototype prototype) {
        return prototype.clone();
    }
}
