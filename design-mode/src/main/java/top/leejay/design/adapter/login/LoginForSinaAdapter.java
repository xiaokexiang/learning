package top.leejay.design.adapter.login;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class LoginForSinaAdapter implements LoginAdapter {
    @Override
    public boolean support(Object object) {

        return object instanceof LoginForSinaAdapter;
    }

    @Override
    public String login(String id, Object adapter) {
        return "Login for Sina success!";
    }
}
