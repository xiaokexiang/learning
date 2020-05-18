package top.leejay.design.delegate.example;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * leader不干活但是要委派任务, 并且leader也是员工
 */
public class Leader implements IEmployee {

    private Map<String, IEmployee> tasks = new HashMap<>();

    public Leader() {
        // Leader知道员工的能力
        tasks.put("登录", new EmployeeA());
        tasks.put("加密", new EmployeeB());
    }

    @Override
    public void doing(String command) {
        System.out.println("Leader 将任务: " + command + "分给" + tasks.get(command).getClass().getName());
        tasks.get(command).doing(command);
    }
}
