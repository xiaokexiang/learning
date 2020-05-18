package top.leejay.design.delegate.example;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class EmployeeB implements IEmployee {
    @Override
    public void doing(String command) {
        System.out.println("我是员工B, 我准备完成" + command + "工作");
    }
}
