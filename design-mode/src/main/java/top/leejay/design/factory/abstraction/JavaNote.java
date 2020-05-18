package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class JavaNote implements INote {
    @Override
    public void edit() {
        System.out.println("编写JAVA笔记");
    }
}
