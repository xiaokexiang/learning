package top.leejay.design.prototype;


import com.google.common.collect.Lists;

/**
 * @author xiaokexiang
 * @date 11/5/2019
 * 
 */
public class PrototypeTest {
    public static void main(String[] args) {
        ConcretePrototypeA prototype = new ConcretePrototypeA();

        prototype.setAge(20);
        prototype.setName("world");
        prototype.setHobbies(Lists.newArrayList("look"));

        System.out.println(prototype);

        Client client = new Client(prototype);
        ConcretePrototypeA prototypeClone = (ConcretePrototypeA) client.startClone(prototype);

        System.out.println(prototypeClone);

        System.out.println("原对象的引用地址" + prototype.getHobbies());
        System.out.println("克隆对象的引用地址" + prototypeClone.getHobbies());
        System.out.println("对象地址比较" + (prototype.getHobbies() == prototypeClone.getHobbies()));


        System.out.println("----------------------------");


        QiTianDaSheng qiTianDaSheng = new QiTianDaSheng();
        QiTianDaSheng clone = (QiTianDaSheng) qiTianDaSheng.clone();
        System.out.println("深度克隆: " + (qiTianDaSheng.jinGuBang == clone.jinGuBang));

        QiTianDaSheng qiTianDaSheng1 = new QiTianDaSheng();
        QiTianDaSheng shallowClone = qiTianDaSheng1.shallowClone(qiTianDaSheng1);
        System.out.println("浅克隆: " + (qiTianDaSheng1.jinGuBang == shallowClone.jinGuBang));
    }
}
