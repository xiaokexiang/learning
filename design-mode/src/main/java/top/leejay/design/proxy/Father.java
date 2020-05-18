package top.leejay.design.proxy;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 代理类
 */
public class Father implements Person {
    private Person person;

    public Father(Person person) {
        this.person = person;
    }

    @Override
    public void findLove() {
        System.out.println("父亲帮儿子相亲");
        person.findLove();
        System.out.println("条件符合,牵手成功");
    }

    public static void main(String[] args) {
        Son son = new Son();
        Father father = new Father(son);
        father.findLove();
    }
}
