package top.leejay.design.delegate.example;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 员工接口
 */
public interface IEmployee {
    /**
     * 员工做的事情
     *
     * @param command 命令
     */
    void doing(String command);
}
