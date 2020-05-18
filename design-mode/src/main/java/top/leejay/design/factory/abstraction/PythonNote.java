package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class PythonNote implements INote {
    @Override
    public void edit() {
        System.out.println("正在编写python笔记");
    }
}
