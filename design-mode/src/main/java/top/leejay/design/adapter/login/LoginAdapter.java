package top.leejay.design.adapter.login;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 登录适配器
 */
public interface LoginAdapter {
    boolean support(Object object);

    String login(String id, Object adapter);
}
