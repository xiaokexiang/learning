package top.leejay.design.adapter.login;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class PassportForThirdAdapter extends SignInService implements PassportForThird {
    @Override
    public String loginForQq(String id) {
        return processLogin(id, LoginForQqAdapter.class);
    }

    @Override
    public String loginForSina(String id) {
        return processLogin(id, LoginForSinaAdapter.class);
    }

    private String processLogin(String key, Class<? extends LoginAdapter> clazz) {
        try {
            LoginAdapter loginAdapter = clazz.newInstance();
            if (loginAdapter.support(loginAdapter)) {
                return loginAdapter.login(key, loginAdapter);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}

class AdapterTest {
    public static void main(String[] args) {
        PassportForThird passport = new PassportForThirdAdapter();
        String s = passport.loginForQq("");
        System.out.println(s);
    }
}
