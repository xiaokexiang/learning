package top.leejay.design.prototype;

import lombok.Data;
import java.util.List;

/**
 * @author xiaokexiang
 * @date 11/5/2019
 * 
 */
@Data
public class ConcretePrototypeA implements Prototype {

    private int age;
    private String name;
    private List<String> hobbies;


    @Override
    public Prototype clone() {
        ConcretePrototypeA prototype = new ConcretePrototypeA();
        prototype.setAge(age);
        prototype.setName(name);
        prototype.setHobbies(hobbies);
        return prototype;
    }
}
